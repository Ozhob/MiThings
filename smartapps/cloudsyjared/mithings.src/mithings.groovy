/**
 *  MiLight Installer
 *
 *  Copyright 2016 Jared Jensen / jared at cloudsy com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "MiThings",
    namespace: "cloudsyjared",
    parent: "cloudsyjared:MiLight Manager",
    author: "Jared Jensen",
    description: "Child application for MiLight Manager -- do not install directly",
    category: "My Apps",
    iconUrl: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn.png",
    iconX2Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png",
    iconX3Url: "http://cdn.device-icons.smartthings.com/Lighting/light20-icn@2x.png")


preferences {
	page(name: "selectMiLight")
}


def getName(myName, n) {
	def result = input myName, "text", title: "Zone #$n Name", required: true
}

def selectMiLight() {
	dynamicPage(name: "selectMiLight", title: "MiLight Wifi Hub Setup", uninstall: true, install: true) {
		section("") {
            input "miLightName", "text", title: "MiLight hub name", description: "ie: Living Room Master Switch", required: true, submitOnChange: false
            input "macAddress", "text", title: "Hub MAC address", description: "Use format AA:BB:CC:DD:EE:FF", required: true, submitOnChange: false
			input "howMany", "number", title: "How many zones?", required: true, submitOnChange: true
            //input "createMaster", "bool", title: "Create master device?", description: "Controlls all the zones on the hub"
		}
		section("Zones") {
			for (int i = 0; i < howMany; i++) {
				def thisName = "dName$i"
				getName(thisName, i + 1)
                paragraph(" ")
			}
		}
	}
}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
	
    state.myDevices = [:]
    
    app.updateLabel("${settings.miLightName}")
    
    def deviceId = "${settings.macAddress}/0"
    def myDevice = getChildDevice(deviceId)
 	if(!myDevice) def childDevice = addChildDevice("cloudsyjared", "MiLight Controller", deviceId, null, [label: "${settings.miLightName}", completedSetup: true])
	myDevice = getChildDevice(deviceId)
    myDevice.name = settings.miLightName
    myDevice.label = settings.miLightName
    myDevice.setPreferences(["mac": "${settings.macAddress}", "group":0, "isDebug": false])
    
	for (int i = 0 ; i < howMany; i++) {
        def thisName = settings.find {it.key == "dName$i"}
    	deviceId = "${settings.macAddress}/${i+1}"
        myDevice = getChildDevice(deviceId)
 		if(!myDevice) def childDevice = addChildDevice("cloudsyjared", "MiLight Controller", deviceId, null, [label: thisName.value, completedSetup: true])
		myDevice = getChildDevice(deviceId)
        myDevice.name = thisName.value
        myDevice.label = thisName.value
        myDevice.setPreferences(["mac": "${settings.macAddress}", "group":i + 1, "isDebug": false])
    }
}

def uninstalled() {
    removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
    delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}