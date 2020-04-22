package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.sdk.iot.device.DeviceClient;
import com.microsoft.azure.sdk.iot.device.IotHubClientProtocol;
import com.microsoft.azure.sdk.iot.device.IotHubEventCallback;
import com.microsoft.azure.sdk.iot.device.IotHubStatusCode;
import com.microsoft.azure.sdk.iot.device.Message;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClient;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationCallback;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientRegistrationResult;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientStatus;
import com.microsoft.azure.sdk.iot.provisioning.device.ProvisioningDeviceClientTransportProtocol;
import com.microsoft.azure.sdk.iot.provisioning.device.internal.exceptions.ProvisioningDeviceClientException;
import com.microsoft.azure.sdk.iot.provisioning.security.SecurityProviderSymmetricKey;
import com.microsoft.samples.iot.opensense.dto.SenseBoxValues;
import com.microsoft.samples.iot.opensense.dto.SensorBoxSensorValue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;

public class IoTHubSender {

    private static final Log logger = LogFactory.getLog(IoTHubSender.class);

    private static final int D2C_MESSAGE_TIMEOUT = 2000; // 2 seconds

    private static Map<String, BiConsumer<Double, SensorBoxValuesMessage>> MAPPER = new HashMap<>();

    static {
        MAPPER.put("Temperatur", (value, message) -> message.setTemperature(value));
        MAPPER.put("rel. Luftfeuchte", (value, message) -> message.setHumidity(value));
        MAPPER.put("Luftdruck", (value, message) -> message.setAirpressure(value));
        MAPPER.put("Beleuchtungsstärke", (value, message) -> message.setIllumination(value));
        MAPPER.put("UV-Intensität", (value, message) -> message.setUvradiation(value));
        MAPPER.put("PM10", (value, message) -> message.setPm10(value));
        MAPPER.put("PM2.5", (value, message) -> message.setPm25(value));
    }

    private DeviceClient deviceClient;

    private ObjectMapper objectMapper = new ObjectMapper();

    private SenseBoxValues lastSent;

    private boolean sendOnlyNewer = true;

    /**
     * 
     * @param latestValue
     */
    public void sendLatestValues(@NonNull final SenseBoxValues latestValue) {

        if (this.willSend(latestValue)) {
            this.doSendLatestValues(latestValue);
            logger.info("Measurements from Sensor Box with ID '" + latestValue.getId() + "' and timestamp '"
                    + latestValue.getLastMeasurementAt() + "' sent");
        } else {
            logger.info("No newer measurements from Sensor Box with ID '" + latestValue.getId() + "' available");
        }
    }

    protected void doSendLatestValues(@NonNull final SenseBoxValues latestValue) {

        SensorBoxValuesMessage boxMsg = this.createPayloadMessageFor(latestValue);
        Message msg;
        try {
            msg = new Message(this.objectMapper.writeValueAsString(boxMsg));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        msg.setContentTypeFinal("application/json");
        msg.setProperty("sensorboxid", latestValue.getId());
        msg.setProperty("sensorboxmodel", latestValue.getModel());
        msg.setMessageId(java.util.UUID.randomUUID().toString());
        msg.setExpiryTime(D2C_MESSAGE_TIMEOUT);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("this", this);
        context.put("latestValue", latestValue);
        context.put("message", msg);
        this.deviceClient.sendEventAsync(msg, new EventCallback(), context);
    }

    /**
     * 
     * @return
     */
    public boolean hasAlreadySent() {
        return this.lastSent != null;
    }

    /**
     * 
     * @param latestValue
     * @return
     */
    public boolean willSend(@NonNull final SenseBoxValues latestValue) {

        return !this.hasAlreadySent() || this.lastSentIsNotActual(latestValue);
    }

    /**
     * 
     * @return
     */
    public Date lastMeasurementTimestamp() {

        return this.lastSent != null ? this.lastSent.getLastMeasurementAt() : null;
    }

    private boolean lastSentIsNotActual(final SenseBoxValues latestValue) {

        return this.lastSent.getLastMeasurementAt().before(latestValue.getLastMeasurementAt());
    }

    private static class EventCallback implements IotHubEventCallback {
        public void execute(IotHubStatusCode status, Object context) {

            Map<String, Object> contextMap = (Map<String, Object>) context;
            Message msg = (Message) contextMap.get("message");

            logger.debug("IoT Hub responded to message " + msg.getMessageId() + " with status " + status.name());

            if (status == IotHubStatusCode.MESSAGE_CANCELLED_ONCLOSE) {
                logger.error("IoT Hub responded with cancelled on close");
            } else {
                IoTHubSender sender = (IoTHubSender) contextMap.get("this");
                sender.lastSent = (SenseBoxValues) contextMap.get("latestValue");
            }
        }
    }

    private SensorBoxValuesMessage createPayloadMessageFor(@NonNull final SenseBoxValues latestValue) {

        NumberFormat format = NumberFormat.getInstance(Locale.US);

        SensorBoxValuesMessage msg = new SensorBoxValuesMessage(latestValue.getId());
        msg.setLastMeasurementAt(latestValue.getLastMeasurementAt());

        if (latestValue.getSensors() != null) {
            for (SensorBoxSensorValue sensorValue : latestValue.getSensors()) {
                double value = 0.0;
                boolean skip = sensorValue.getLastMeasurement() == null;
                if (skip) {
                    logger.error("Sensor '" + sensorValue.getTitle() + "' has no last measurement");
                    continue;
                }

                try {
                    value = format.parse(sensorValue.getLastMeasurement().getValue()).doubleValue();
                } catch (ParseException e) {
                    logger.error("Unexpected value '" + sensorValue.getLastMeasurement().getValue()
                            + "' for sensor with title '" + sensorValue.getTitle());
                    continue;
                }

                if (MAPPER.containsKey(sensorValue.getTitle()))
                    MAPPER.get(sensorValue.getTitle()).accept(value, msg);
                else {
                    logger.debug("Unknown sensor value with title '" + sensorValue.getTitle() + "' found");
                }
            }
        }

        return msg;
    }

    private static final int MAX_TIME_TO_WAIT_FOR_REGISTRATION = 10000; // in milli seconds

    public void setupWithDPS(String dpsScopeID, String deviceID, String deviceKey) throws IOException {

        DeviceClient iothubClient = null;
        SecurityProviderSymmetricKey securityClientSymmetricKey = new SecurityProviderSymmetricKey(
                deviceKey.getBytes(), deviceID);
        ProvisioningDeviceClient provisioningDeviceClient = null;

        try {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            provisioningDeviceClient = ProvisioningDeviceClient.create("global.azure-devices-provisioning.net",
                    dpsScopeID, ProvisioningDeviceClientTransportProtocol.HTTPS, securityClientSymmetricKey);
            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(),
                    provisioningStatus);
            while (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
                    .getProvisioningDeviceClientStatus() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
                if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
                        .getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR
                        || provisioningStatus.provisioningDeviceClientRegistrationInfoClient
                                .getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED
                        || provisioningStatus.provisioningDeviceClientRegistrationInfoClient
                                .getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED) {
                    provisioningStatus.exception.printStackTrace();
                    System.out.println("Registration error, bailing out");
                    break;
                }
                System.out.println("Waiting for Provisioning Service to register");
                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.provisioningDeviceClientRegistrationInfoClient
                    .getProvisioningDeviceClientStatus() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
                        
                System.out.println("IotHUb Uri : "
                        + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri());
                System.out.println("Device ID : "
                        + provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId());

                // connect to iothub
                String iotHubUri = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getIothubUri();
                String deviceId = provisioningStatus.provisioningDeviceClientRegistrationInfoClient.getDeviceId();
                try {
                    iothubClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId,
                            securityClientSymmetricKey, IotHubClientProtocol.AMQPS);
                    iothubClient.open();
                } catch (IOException | URISyntaxException e) {
                    System.out.println("Device client threw an exception: " + e.getMessage());
                    if (iothubClient != null) {
                        iothubClient.closeNow();
                    }
                }
                this.deviceClient = iothubClient;
            }
        } catch (ProvisioningDeviceClientException | InterruptedException e) {

            System.out.println("Provisioning Device Client threw an exception" + e.getMessage());
            if (provisioningDeviceClient != null) {
                provisioningDeviceClient.closeNow();
            }

            throw new RuntimeException(e);
        }

        if (provisioningDeviceClient != null) {
            provisioningDeviceClient.closeNow();
        }
    }

    static class ProvisioningStatus
    {
        ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationInfoClient = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl implements ProvisioningDeviceClientRegistrationCallback
    {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult, Exception exception, Object context)
        {
            if (context instanceof ProvisioningStatus)
            {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.provisioningDeviceClientRegistrationInfoClient = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            }
            else
            {
                System.out.println("Received unknown context");
            }
        }
    }

    public DeviceClient getDeviceClient() {
        return deviceClient;
    }

    public void setDeviceClient(DeviceClient deviceClient) {
        this.deviceClient = deviceClient;
    }

    public boolean isSendOnlyNewer() {
        return sendOnlyNewer;
    }

    public void setSendOnlyNewer(boolean sendOnlyNewer) {
        this.sendOnlyNewer = sendOnlyNewer;
    }
}