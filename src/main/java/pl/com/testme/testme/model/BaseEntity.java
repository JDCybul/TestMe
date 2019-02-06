package pl.com.testme.testme.model;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Data
@MappedSuperclass
class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private LocalDateTime createDate = LocalDateTime.now();

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntity.class);


}
