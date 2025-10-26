package com.alphapay.payEngine.cache.service;

import org.springframework.stereotype.Service;

@Service
public class ApplicationPublicKey {/*
    // Changed to ConcurrentHashMap for thread safety in concurrent read/write scenarios
    private final Map<String, String> applicationPublicKeyMap = new ConcurrentHashMap<>();
    // Logger is made static final as per convention for loggers
    private static final Logger logger = LoggerFactory.getLogger(ApplicationPublicKey.class);


    @Autowired
    ApplicationPublicKeyRepo applicationPublicKeyRepo;

    @Autowired
    public Map<String, String> getApplicationPublicKeyMap() {
        // Check if the map is already populated to avoid unnecessary DB calls
        if (applicationPublicKeyMap.isEmpty()) {
            Iterable<CashedApplicationPublicKey> iterable = applicationPublicKeyRepo.findAll();
            // Java 8 forEach to populate the map

            Iterator<CashedApplicationPublicKey> iterator = iterable.iterator();
            while (iterator.hasNext()) {
                CashedApplicationPublicKey cashedKey = iterator.next();
                if (cashedKey != null) {
                    applicationPublicKeyMap.put(cashedKey.getAppId(), cashedKey.getPublicKey());
                    logger.debug("Public keys loaded from cache.");
                }
            }
        }
        // Returning a copy of the map to prevent exposing the internal map structure
        return new HashMap<>(applicationPublicKeyMap);
    }

    public void resetApplicationPublicKeyMap(HashMap<String, String> newApplicationPublicKeyMap) {
        applicationPublicKeyRepo.deleteAll();
        logger.debug("All public keys are deleted and should be updated");

        // Using Java 8 Stream API for transforming and collecting entries to CashedApplicationPublicKey objects
        List<CashedApplicationPublicKey> publicKeyList = new ArrayList<>();
        newApplicationPublicKeyMap.forEach((key, value) -> publicKeyList.add(new CashedApplicationPublicKey(key, value)));


        applicationPublicKeyRepo.saveAll(publicKeyList);
        logger.debug("All public keys are saved successfully");

        applicationPublicKeyMap.clear();
        applicationPublicKeyMap.putAll(newApplicationPublicKeyMap);

    }

    public String findByKey(String key) {
        return applicationPublicKeyMap.get(key);
    }

    public void setApplicationPublicKeyMap(String key, String value) {
        // Simplifying the update and insert logic using Optional
        CashedApplicationPublicKey cashedPublicKey = applicationPublicKeyRepo.findById(key)
                .orElse(new CashedApplicationPublicKey(key, value)); // Handling both update and insert cases

        cashedPublicKey.setPublicKey(value);
        applicationPublicKeyRepo.save(cashedPublicKey);
        // Updating the map with the new value
        applicationPublicKeyMap.put(key, value);

        // Logging whether a new key was added or an existing one was updated
        logger.debug("add/ or update new public key to {}", key);
    }
*/
}
