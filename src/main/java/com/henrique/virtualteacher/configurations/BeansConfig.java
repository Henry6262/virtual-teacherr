package com.henrique.virtualteacher.configurations;

import com.henrique.virtualteacher.entities.Comment;
import com.henrique.virtualteacher.models.CommentModel;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;


@Configuration
@PropertySource("classpath:application.properties")
public class BeansConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

//        mapper.createTypeMap(Comment.class, CommentModel.class)
//                .addMappings(modelMapper -> modelMapper.map(source -> source.getCourse().getId(), CommentModel::setCourseId))
//                .addMapping(modelMapper -> modelMapper.getUser().getId(), CommentModel::setUserId);

        return mapper;
    }

    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("VirtualTeacher");
    }


}
