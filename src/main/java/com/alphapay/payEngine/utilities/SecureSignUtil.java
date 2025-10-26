package com.alphapay.payEngine.utilities;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates the secure HMAC signature exactly as described in the
 * "Creating a Secure Signature" section of the MBME Pay integration guide.
 */
@Slf4j
public final class SecureSignUtil {
    private SecureSignUtil() {}

    public static String generateSecureSign(Map<String,Object> payload,
                                     String secretKey,
                                     String algorithm) {
        log.debug("Payload        : {},secret key   :{}, algorithm  {}", payload,secretKey,algorithm);
        Map<String,String> flat = new LinkedHashMap<>();
        flatten("", payload, flat);

        List<String> keys = new ArrayList<>(flat.keySet());
        Collections.sort(keys);

        StringBuilder raw = new StringBuilder();
        for (String k : keys) {
            if (raw.length() > 0) raw.append('&');
            String v = flat.get(k) == null ? "" : flat.get(k);
            raw.append(k).append('=').append(v);
        }
        log.debug("Raw string      : {}", raw);


        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8); // ⚠ key as plain text
        return hmacHex(raw.toString(), keyBytes, algorithm);
    }

    /** Algorithm-selectable */
    public static String generateSecureSignForVerification(Map<String, ?> payload,
                                            String secretKey,
                                            String algorithm) {

        // 1-2. flatten + normalise
        SortedMap<String, String> sorted = new TreeMap<>();
        flattenAndNormalise("", payload, sorted);

        // 3-4. build raw string
        String raw = sorted.entrySet().stream()
                .map(e -> e.getKey() + '=' + e.getValue())
                .collect(Collectors.joining("&"));

        log.debug("Raw string for HMAC ({}): {}", algorithm, raw);

        // 5. HMAC
        return hmacHex(raw,
                secretKey.getBytes(StandardCharsets.UTF_8),
                algorithm);
    }

    private static void flatten(String prefix, Object val, Map<String,String> out) {
        if (val == null) {
            out.put(prefix, "");
        } else if (val instanceof Map<?,?> m) {
            m.forEach((k,v) -> flatten(concat(prefix,k.toString()), v, out));
        } else if (val instanceof List<?> l) {
            for (int i=0;i<l.size();i++)
                flatten(concat(prefix,"["+i+"]"), l.get(i), out);
        } else {
            out.put(prefix, val.toString());
        }
    }

    @SuppressWarnings("unchecked")
    private static void flattenAndNormalise(String prefix,
                                            Object value,
                                            Map<String, String> out) {

        if (value == null) {
            out.put(normalise(prefix), "");
            return;
        }

        /* --- Map --------------------------------------------------------- */
        if (value instanceof Map<?,?> map) {
            map.forEach((k, v) ->
                    flattenAndNormalise(concat(prefix, k.toString()), v, out));
            return;
        }

        /* --- List / array ------------------------------------------------- */
        if (value instanceof List<?> list) {
            for (int i = 0; i < list.size(); i++) {
                flattenAndNormalise(concat(prefix, "[" + i + "]"),
                        list.get(i), out);
            }
            return;
        }

        /* --- Primitive / leaf -------------------------------------------- */
        out.put(normalise(prefix), value.toString());
    }


    /**
     * Convert bracket notation to dot notation, e.g.
     *   transaction_info[currency] -> transaction_info.currency
     *   items[0].id               -> items.0.id
     */
    private static String normalise(String key) {
        return key.replace('[', '.').replace("]", "");
    }

    private static String concat(String prefix, String k) {
        return prefix.isEmpty() ? k : prefix + '.' + k;
    }

    private static String hmacHex(String data, byte[] key, String alg) {
        try {
            Mac mac = Mac.getInstance(alg);
            mac.init(new SecretKeySpec(key, alg));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length*2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b>>4)&0xF,16))
                        .append(Character.forDigit(b & 0xF,16));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to compute HMAC", e);
        }
    }
}
