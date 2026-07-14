package com.yumg.starter.common.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.yumg.starter.entities.Announcement;
import com.yumg.starter.entities.AuditEvent;
import com.yumg.starter.entities.IpAccessRule;
import com.yumg.starter.entities.RefreshSession;
import com.yumg.starter.entities.User;
import jakarta.persistence.Convert;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstantPersistenceMappingTest {

    @Test
    void instantPropertiesUsePortableIsoTextConverter() {
        List<Class<?>> persistentTypes = List.of(AuditedEntity.class, Announcement.class,
                AuditEvent.class, IpAccessRule.class, RefreshSession.class, User.class);

        List<String> nonPortableFields = persistentTypes.stream()
                .flatMap(type -> List.of(type.getDeclaredFields()).stream())
                .filter(field -> field.getType().equals(Instant.class))
                .filter(field -> !field.isAnnotationPresent(Convert.class)
                        || !field.getAnnotation(Convert.class).converter().getSimpleName()
                        .equals("InstantStringConverter"))
                .map(Field::getName)
                .toList();

        assertThat(nonPortableFields).isEmpty();
    }
}
