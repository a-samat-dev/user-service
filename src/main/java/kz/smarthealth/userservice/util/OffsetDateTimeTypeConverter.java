package kz.smarthealth.userservice.util;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class OffsetDateTimeTypeConverter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonElement jsonElement,
                                      Type type,
                                      JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement == null)
            return null;

        String[] split = jsonElement.getAsString().split(" ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        LocalDateTime dateTime = LocalDateTime.parse(split[0] + " " + split[1], formatter);
        int[] hourAndMinute = getHourAndMinute(split[2]);

        return OffsetDateTime.of(dateTime, ZoneOffset.ofHoursMinutes(hourAndMinute[0], hourAndMinute[1]));
    }

    private int[] getHourAndMinute(String value) {
        value = value.replaceAll("\\+", "");
        value = value.replaceAll("-", "");
        String[] ss = value.split(":");

        return new int[]{Integer.parseInt(value.substring(0, 2)), Integer.parseInt(value.substring(2, 4))};
    }

    @Override
    public JsonElement serialize(OffsetDateTime offsetDateTime,
                                 Type type,
                                 JsonSerializationContext jsonSerializationContext) {
        if (offsetDateTime == null)
            return null;

        return new JsonPrimitive(offsetDateTime.toString());
    }
}
