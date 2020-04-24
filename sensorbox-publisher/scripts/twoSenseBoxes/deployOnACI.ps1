az group create --name senseboxpublishers --location northeurope

az group deployment create --resource-group senseboxpublishers --template-file azuredeploy.json

az container show --resource-group senseboxpublishers --name senseboxContainerGroup --output table

az container logs --resource-group senseboxpublishers --name senseboxContainerGroup --container-name vienna-publisher

az container logs --resource-group senseboxpublishers --name senseboxContainerGroup --container-name munich-publisher