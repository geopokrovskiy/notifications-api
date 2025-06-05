package com.geopokrovskiy.utils;

public class TestUtils {

    public static final String createTopicRequest = "{\n" +
            "    \"topic_name\": \"sign_up\"\n" +
            "}";


    public static final String createSchemaRequest = "{\n" +
            "    \"schema\": \"{\\\"type\\\": \\\"record\\\", \\\"name\\\": \\\"Notification\\\"," +
            " \\\"namespace\\\": \\\"com.example.notifications\\\", " +
            "\\\"fields\\\": [{\\\"name\\\": \\\"subject\\\", \\\"type\\\": \\\"string\\\"}," +
            " {\\\"name\\\": \\\"message\\\", \\\"type\\\": \\\"string\\\"}, " +
            "{\\\"name\\\": \\\"notification_type\\\", \\\"type\\\": {\\\"type\\\": \\\"enum\\\", " +
            "\\\"name\\\": \\\"NotificationType\\\", \\\"symbols\\\": [\\\"EMAIL\\\", \\\"PHONE\\\"]}}, " +
            "{\\\"name\\\": \\\"notification_dest\\\", \\\"type\\\": \\\"string\\\"}, " +
            "{\\\"name\\\": \\\"created_by\\\", \\\"type\\\": \\\"string\\\"}]}\"\n" +
            "}";


    public static final String createNotificationRequest = "{\n" +
            "  \"value_schema_id\": \"1\",\n" +
            "  \"records\": [\n" +
            "    {\n" +
            "      \"value\": {\n" +
            "        \"subject\": \"Test\",\n" +
            "        \"message\": \"It's a test message. Please ignore it.\",\n" +
            "        \"notification_type\": \"EMAIL\",\n" +
            "        \"notification_dest\": \"email@test.com\",\n" +
            "        \"created_by\": \"SYSTEM\"\n" +
            "      }\n" +
            "    }\n" +
            "  ]\n" +
            "}";

}
