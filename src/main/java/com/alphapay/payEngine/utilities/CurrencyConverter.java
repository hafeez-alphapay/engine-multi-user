package com.alphapay.payEngine.utilities;

import com.alphapay.payEngine.utilities.CurrencyLabel;
import org.apache.commons.lang3.StringUtils;

import java.util.Currency;
import java.util.HashMap;
import java.util.Optional;

public class CurrencyConverter {

    private static final HashMap<Integer, CurrencyLabel> currencyMap = new HashMap<>();

    static {
        // Major Currencies
        currencyMap.put(840, new CurrencyLabel("USD", "دولار أمريكي"));
        currencyMap.put(978, new CurrencyLabel("EUR", "يورو"));
        currencyMap.put(826, new CurrencyLabel("GBP", "جنيه إسترليني"));
        currencyMap.put(392, new CurrencyLabel("JPY", "ين ياباني"));
        currencyMap.put(156, new CurrencyLabel("CNY", "يوان صيني"));

        // GCC Currencies
        currencyMap.put(784, new CurrencyLabel("AED", "درهم إماراتي"));
        currencyMap.put(414, new CurrencyLabel("KWD", "دينار كويتي"));
        currencyMap.put(682, new CurrencyLabel("SAR", "ريال سعودي"));
        currencyMap.put(512, new CurrencyLabel("OMR", "ريال عماني"));
        currencyMap.put(634, new CurrencyLabel("QAR", "ريال قطري"));
        currencyMap.put(48,  new CurrencyLabel("BHD", "دينار بحريني"));

        // African Currencies
        currencyMap.put(710, new CurrencyLabel("ZAR", "راند جنوب أفريقي"));
        currencyMap.put(566, new CurrencyLabel("NGN", "نايرا نيجيري"));
        currencyMap.put(404, new CurrencyLabel("KES", "شلن كيني"));
        currencyMap.put(818, new CurrencyLabel("EGP", "جنيه مصري"));
        currencyMap.put(504, new CurrencyLabel("MAD", "درهم مغربي"));
        currencyMap.put(938, new CurrencyLabel("SDG", "جنيه سوداني"));

        // Other popular currencies
        currencyMap.put(36,  new CurrencyLabel("AUD", "دولار أسترالي"));
        currencyMap.put(124, new CurrencyLabel("CAD", "دولار كندي"));
        currencyMap.put(643, new CurrencyLabel("RUB", "روبل روسي"));
    }

    public static CurrencyLabel convertToLabel(String currencyCode) {
        if(StringUtils.isNumeric(currencyCode))
        {
            int code=Integer.parseInt(currencyCode);
            return currencyMap.get(code);
        }

        // Find the currency using Java's Currency to parse String Code and validate it
        Optional<Currency> currency = Currency.getAvailableCurrencies().stream()
                .filter(c -> c.getCurrencyCode().equalsIgnoreCase(currencyCode))
                .findAny();

        int code= currency.map(Currency::getNumericCode).orElse(null);
        return currencyMap.get(code);

    }

}
