package com.henrique.virtualteacher.configurations;

import com.henrique.virtualteacher.entities.VerificationToken;
import com.henrique.virtualteacher.entities.Wallet;
import com.henrique.virtualteacher.models.VerificationTokenModel;
import com.henrique.virtualteacher.models.WalletModel;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:application.properties")
public class BeansConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        mapper.createTypeMap(Wallet.class, WalletModel.class)
                .addMappings(modelMapper -> modelMapper.map(scr -> scr.getOwner().getId(), WalletModel::setUserId))
                .addMapping(modelMapper -> modelMapper.getOwner().getId(), WalletModel::setUserId);

        mapper.createTypeMap(VerificationToken.class, VerificationTokenModel.class)
                .addMappings(modelMapper -> modelMapper.map(src -> src.getVerifier().getId(), VerificationTokenModel::setVerifierId));

        return mapper;
    }

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("VirtualTeacher");
    }


}
