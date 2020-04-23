package com.microsoft.samples.iot.sensorbox;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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
import com.microsoft.azure.sdk.iot.device.DeviceTwin.Property;
import com.microsoft.azure.sdk.iot.device.DeviceTwin.TwinPropertyCallBack;
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

    public void open() throws IOException {

        this.deviceClient.open();
        this.deviceClient.startDeviceTwin(new DeviceTwinStatusCallBack(), null, new TwinPropertyCallBack(){
            @Override
            public void TwinPropertyCallBack(Property property, Object context) {
                logger.info("Desired property '" + property.getKey() + "' changed to '" + property.getValue() + "'. Ignoring");
            }
        }, null);
    }

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

    @SuppressWarnings("unchecked")
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

    // DeviceTwin status updates
    protected static class DeviceTwinStatusCallBack implements IotHubEventCallback {
        @Override
        public void execute(IotHubStatusCode status, Object context) {
            if (status != IotHubStatusCode.OK_EMPTY)
                logger.debug("IoT Hub responded to device twin operation with status " + status.name());
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

    /**
     * 
     */
    public void setupWithDPS(@NonNull final String dpsScopeID, @NonNull final String deviceID,
            @NonNull final String deviceKey) throws IOException {

        DeviceClient iothubClient = null;
        SecurityProviderSymmetricKey securityClientSymmetricKey = new SecurityProviderSymmetricKey(deviceKey.getBytes(),
                deviceID);
        ProvisioningDeviceClient provisioningDeviceClient = null;

        try {
            ProvisioningStatus provisioningStatus = new ProvisioningStatus();

            provisioningDeviceClient = ProvisioningDeviceClient.create("global.azure-devices-provisioning.net",
                    dpsScopeID, ProvisioningDeviceClientTransportProtocol.HTTPS, securityClientSymmetricKey);
            provisioningDeviceClient.registerDevice(new ProvisioningDeviceClientRegistrationCallbackImpl(),
                    provisioningStatus);

            while (provisioningStatus.status() != ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {
                if (provisioningStatus.status() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ERROR
                        || provisioningStatus
                                .status() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_DISABLED
                        || provisioningStatus
                                .status() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_FAILED) {

                    logger.error("Device provisioning error", provisioningStatus.exception);
                    throw new IOException("Device provisioning error", provisioningStatus.exception);
                }

                Thread.sleep(MAX_TIME_TO_WAIT_FOR_REGISTRATION);
            }

            if (provisioningStatus.status() == ProvisioningDeviceClientStatus.PROVISIONING_DEVICE_STATUS_ASSIGNED) {

                logger.info("Device provisioned with ID '" + provisioningStatus.result.getDeviceId()
                        + "' and registered at IoT Hub '" + provisioningStatus.result.getIothubUri() + "'");

                // connect to iothub
                String iotHubUri = provisioningStatus.result.getIothubUri();
                String deviceId = provisioningStatus.result.getDeviceId();
                try {
                    iothubClient = DeviceClient.createFromSecurityProvider(iotHubUri, deviceId,
                            securityClientSymmetricKey, IotHubClientProtocol.AMQPS);
                    iothubClient.open();
                } catch (IOException | URISyntaxException e) {

                    logger.error("Open connection to IoT Hub threw an exception", e);

                    if (iothubClient != null) {
                        iothubClient.closeNow();
                    }
                    if (e instanceof IOException)
                        throw (IOException) e;
                    else
                        throw new IOException("Open connection to IoT Hub threw an exception", e);
                }
                this.deviceClient = iothubClient;
            } else {
                logger.error("Provisioning Device Client returned error ");
                throw new IOException("Provisioning Device Client returned error ");
            }
        } catch (ProvisioningDeviceClientException | InterruptedException e) {

            logger.error("Provisioning Device Client threw an exception", e);
            throw new IOException("Provisioning Device Client threw an exception", e);
        } finally {

            if (provisioningDeviceClient != null) {
                provisioningDeviceClient.closeNow();
            }
        }
    }

    static class ProvisioningStatus {
        ProvisioningDeviceClientRegistrationResult result = new ProvisioningDeviceClientRegistrationResult();
        Exception exception;

        public ProvisioningDeviceClientStatus status() {
            return result.getProvisioningDeviceClientStatus();
        };
    }

    static class ProvisioningDeviceClientRegistrationCallbackImpl
            implements ProvisioningDeviceClientRegistrationCallback {
        @Override
        public void run(ProvisioningDeviceClientRegistrationResult provisioningDeviceClientRegistrationResult,
                Exception exception, Object context) {
            if (context instanceof ProvisioningStatus) {
                ProvisioningStatus status = (ProvisioningStatus) context;
                status.result = provisioningDeviceClientRegistrationResult;
                status.exception = exception;
            } else {
                System.out.println("Received unknown context");
            }
        }
    }

    public void reportSettings(@NonNull final SenseBoxValues fromValues) throws IllegalArgumentException, IOException {

        HashSet<Property> set = new HashSet<>();
        set.add(new Property("manufacturer", "SenseBox")); // (manufacturer)
        set.add(new Property("model", fromValues.getModel())); // (model)
        //set.add(new Property("swVersion", "text")); // (swVersion)
        //set.add(new Property("osName", "text")); // (osName)
        //set.add(new Property("processorArchitecture", "text")); // (processorArchitecture)
        //set.add(new Property("processorManufacturer", "text")); // (processorManufacturer)
        //set.add(new Property("totalStorage", 1)); // (totalStorage) <- try another value!
        //set.add(new Property("totalMemory", 2)); // (totalMemory) <- try another value!
        //set.add(new Property("currentlocation", 10)); // (currentlocation) <- try another value!
        set.add(new Property("name", fromValues.getName())); // (name)
        set.add(new Property("_id", fromValues.getId())); // (_id)
        set.add(new Property("grouptag", fromValues.getGrouptag())); // (grouptag)
        set.add(new Property("exposure", fromValues.getExposure())); // (exposure) <- try another value!
        set.add(new Property("createdAt", fromValues.getCreatedAt())); // (createdAt) <- try another value!
        set.add(new Property("updatedAt", fromValues.getUpdatedAt()));// (updatedAt) <- try another value!

        this.deviceClient.sendReportedProperties(set);
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