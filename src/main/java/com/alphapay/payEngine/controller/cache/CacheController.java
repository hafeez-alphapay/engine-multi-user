package com.alphapay.payEngine.controller.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cache")
public class CacheController {

    Logger logger = LoggerFactory.getLogger(CacheController.class);
//    @Autowired
//    OptimusServiceConfigRepository serviceConfigRepository;
//
//    @Autowired
//    OptimusApplicationServiceConfigCachedRepo optimusApplicationServiceConfigCachedRepo;
//
//    @Autowired
//    BackEndServiceRepository backEndServiceRepository;
//
//    @Autowired
//    BackendIntegrationServiceConfigCachedRepo backendCacheRepo;
//
//    @Autowired
//    FinancialInstutionsLoader financialInstutionsLoader;

    @GetMapping ("/reloadAll")
    public ResponseEntity<Void> reload(){
       /* logger.info("Reload optimus service for all front layer services");
        List<OptimusServiceConfig> configs= serviceConfigRepository.findAll();
        if(configs!=null) {
            for(OptimusServiceConfig config:configs) {
                String serviceIdentifierByServiceId = "ByServiceId" + ":" + config.getServiceId() + ":" + config.getApplicationId();
                String serviceIdentifierByServiceName=config.getServiceName()+":"+config.getApplicationId();
                try{
                    optimusApplicationServiceConfigCachedRepo.save(new CachedOptimusApplicationServiceConfig(serviceIdentifierByServiceId,config));
                    optimusApplicationServiceConfigCachedRepo.save(new CachedOptimusApplicationServiceConfig(serviceIdentifierByServiceName,config));
                }
                catch(Throwable ex)
                {
                    logger.error("Unable to save value in cahce due to {}, but will continue execution",ex);
                }
            }
        }
        logger.info("Reload backend service for all integration services");
        Iterable<BackendService> backEndConfigs= backEndServiceRepository.findAll();
        if(backEndConfigs!=null)
        {
            Iterator<BackendService> iter = backEndConfigs.iterator();
            while(iter.hasNext()){
                BackendService service= iter.next();
                try {
                    String serviceIdentifier = service.getServiceName() + ":" + service.getApplicationId();
                    BackendService cachedService = service.clone();
                    backendCacheRepo.save(new BackendIntegrationServiceConfig(serviceIdentifier, cachedService));
                }
                catch(Throwable ex)
                {
                    logger.error("Unable to save value in cahce due to {}, but will continue execution",ex);

                }

            }
        }
        logger.info("Reloading Financial Institution");
        financialInstutionsLoader.loadInstitutions();

*/
        return new ResponseEntity<>(HttpStatus.OK);

    }
}
