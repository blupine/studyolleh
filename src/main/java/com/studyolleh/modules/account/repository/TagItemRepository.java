package com.studyolleh.modules.account.repository;

import com.studyolleh.modules.account.domain.TagItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagItemRepository extends JpaRepository<TagItem, Long> {
}
