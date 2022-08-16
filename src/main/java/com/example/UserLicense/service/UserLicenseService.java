package com.example.UserLicense.service;

import com.example.UserLicense.Feign.OrderFeignClient;
import com.example.UserLicense.LicenseStatus;
import com.example.UserLicense.Order;
import com.example.UserLicense.entity.UserLicense;
import com.example.UserLicense.repository.UserLicenseRepository;
import com.example.UserLicense.repository.UserLicenseRepository1;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class UserLicenseService {

    @Autowired
    private UserLicenseRepository userLicenseRepository;
    private UserLicenseRepository1 userLicenseRepository1;

    @Autowired
    private OrderFeignClient orderFeignClient;

    public String saveUserLicense(Long userId, Long licenseId) {
        List<LicenseStatus> listOfLicenseKey = orderFeignClient.getLicenseById(licenseId);
        UserLicense userLicense = new UserLicense();
        log.info("Inside saverUserLicense");
        if(listOfLicenseKey.size()==0) {
            log.info("Inside if");
            return "Invalid license Id";
        }
            else {
            log.info("Inside else");
            for (LicenseStatus licenseKey:listOfLicenseKey)
            {
                log.info("Inside for");
                if(licenseKey.getLicenseKeyStatus().equals("NOT CONSUMED"))
                {
                    log.info("Inside for each if");
                    orderFeignClient.updateStatus(licenseKey.getLicenseKey());
                    userLicense.setStatus("CONSUMED");
                    userLicense.setUserId(userId);
                    userLicense.setLicenseKey(licenseKey.getLicenseKey());
                    userLicenseRepository.save(userLicense);
//                  Kafka producer code(Send userLicense to Kafka)
                    return "License key associated"+userLicense.getLicenseKey();
                }
            }
        }
        return "No license key available";
        }


    public List<UserLicense> findUserById(Long userId) {
        log.info("Inside saveUser method of UserLicenseService");
        return userLicenseRepository.findByUserId(userId);
    }

    public void deleteUserById(Long userId) {
        log.info("Inside deleteUserById of UserLicenseService");
//      Send message to deleteLicenseKey to the user
        userLicenseRepository1.deleteById(userId);
    }

    public LicenseStatus update(String key) {
        return orderFeignClient.updateStatus(key);
    }

//    public List<Order> listOrder(){
//        return orderFeignClient.listAll();
//    }
//
//    public List<LicenseStatus> listLicense(Long licenseId){
//        return orderFeignClient.getLicenseById(licenseId);
//    }

}