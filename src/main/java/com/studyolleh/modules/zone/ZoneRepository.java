package com.studyolleh.modules.zone;

import com.studyolleh.modules.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone findByCityAndProvince(String city, String province);
}
