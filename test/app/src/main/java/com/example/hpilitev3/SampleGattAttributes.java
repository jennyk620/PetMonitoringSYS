/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.hpilitev3;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class SampleGattAttributes {
    private static HashMap<String, String> attributes = new HashMap();

    private static final String UUID_MASK = "0000%s-0000-1000-8000-00805f9b34fb";
    //////////////////////////////////////////////////////////////////////////////////////////////
    //public static String CLIENT_CHARACTERISTIC_CONFIG = "00002A9D-0000-1000-8000-00805F9B34FB";
    public static String CLIENT_CHARACTERISTIC_CONFIG = String.format(UUID_MASK, "2902");
    //public static String Accelerate_PnR = "00002a98-0000-1000-8000-00805f9b34fb";
    //public static String Accelerate_XYZ = "00002a9d-0000-1000-8000-00805f9b34fb";
    //public static String Gyroscope_XYZ = "00002a9e-0000-1000-8000-00805f9b34fb";
    //public static String Pedometer = "00002a9f-0000-1000-8000-00805f9b34fb";
    //public static String TOTALTEST = "00002aaa-0000-1000-8000-00805f9b34fb";


    // 블루노나노
    //public static String ACCELERATE_SERVICE = "0000dfb2-0000-1000-8000-00805f9b34fb"; // 기존 ACCELERATE_SERVICE
    // NINA
    public static String UUID_SERVICE_SERIAL_PORT       = "2456e1b9-26e2-8f83-e744-f34f01e9d701";
    public static String UUID_CHARACTERISTIC_FIFO       = "2456e1b9-26e2-8f83-e744-f34f01e9d703";
    public static String UUID_CHARACTERISTIC_CREDITS    = "2456e1b9-26e2-8f83-e744-f34f01e9d704";
    /*
    static {
        // Sample Characteristics.
        attributes.put("00002a00-0000-1000-8000-00805f9b34fb", "Manufacturer Name");
        attributes.put(ACCELERATE_SERVICE, "Accelerate Service");
        attributes.put(Accelerate_PnR, "Accelerate PITCH/ROLL");
        attributes.put(Accelerate_XYZ, "Accelerate X/Y/Z");
        attributes.put(Gyroscope_XYZ, "Gyroscope X/Y/Z");
        attributes.put(Pedometer, "Pedometer");
        attributes.put(TOTALTEST,"Totaltest");
    }*/

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}