package com.alphapay.payEngine.utilities;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalCommaDeserializer extends JsonDeserializer<BigDecimal> {
    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText();
        if (value != null) {
            // Remove commas from the string before converting to BigDecimal
            value = value.replace(",", "");
        }
        return new BigDecimal(value);
    }
}
