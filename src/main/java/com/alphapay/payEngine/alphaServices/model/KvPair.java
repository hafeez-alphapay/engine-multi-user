package com.alphapay.payEngine.alphaServices.model;
import jakarta.persistence.*;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class KvPair {

    /** Map value rendered as String (JSON, plain text, etc.). */
    @Column(name = "value")
    private String value;

    /** Optional Java type hint (e.g., "Integer", "Boolean"). */
    @Column(name = "type")
    private String type;

    /* ---------- Convenience helpers ------------------------------------ */

    public static KvPair of(Object obj) {
        return new KvPair(
                obj == null ? null : obj.toString(),
                obj == null ? null : obj.getClass().getSimpleName()
        );
    }

    /** Convert the stored value back to an Object using the `type` hint. */
    public Object toObject() {
        if (value == null) return null;
        return switch (type) {
            case "Integer"  -> Integer.valueOf(value);
            case "Long"     -> Long.valueOf(value);
            case "Boolean"  -> Boolean.valueOf(value);
            case "Double"   -> Double.valueOf(value);
            // add custom cases—or JSON-deserialise complex objects
            default         -> value;          // keep as String fallback
        };
    }
}

