package com.welink.commons.tacker;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.io.CharStreams;
import io.keen.client.java.KeenJsonHandler;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

import static com.alibaba.fastjson.parser.Feature.AllowISO8601DateFormat;
import static com.alibaba.fastjson.serializer.SerializerFeature.UseISO8601DateFormat;

/**
 * Created by saarixx on 9/1/15.
 */
public class FastJsonHandler implements KeenJsonHandler {

    public static String DEFAULT_MAP_KEY_MOBILE = "mobile";

    public static String DEFAULT_MAP_KEY_OBJECT = "data";

    public static TypeReference<Map<String, Object>> DEFAULT_TYPE = new TypeReference<Map<String, Object>>() {
    };

    /**
     * 所有的Object
     *
     * @param reader
     * @return
     * @throws IOException
     */
    @Override
    public Map<String, Object> readJson(Reader reader) throws IOException {
        return JSON.parseObject(CharStreams.toString(reader), DEFAULT_TYPE, AllowISO8601DateFormat);
    }

    @Override
    public void writeJson(Writer writer, Map<String, ?> value) throws IOException {
        JSON.writeJSONStringTo(value, writer, UseISO8601DateFormat);
    }
}
