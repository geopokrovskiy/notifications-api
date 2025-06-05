package com.geopokrovskiy.mapper.mapstruct.notification;

import com.geopokrovskiy.dto.notification_service.notification.NotificationResponseDto;
import com.geopokrovskiy.entity.Notification;
import org.apache.avro.generic.GenericRecord;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification map(NotificationResponseDto notificationResponseDto);
    @InheritInverseConfiguration
    NotificationResponseDto map(Notification notification);

    @Mapping(target = "message", expression = "java( record.get(\"message\").toString())")
    @Mapping(target = "subject", expression = "java( record.get(\"subject\").toString())")
    @Mapping(target = "notificationDest", expression = "java( record.get(\"notification_dest\").toString())")
    @Mapping(target = "createdBy", expression = "java( record.get(\"created_by\").toString())")
    @Mapping(target = "notificationType", expression = "java(NotificationType.valueOf(record.get(\"notification_type\").toString()))")
    Notification fromGenericRecord(GenericRecord record);
}
