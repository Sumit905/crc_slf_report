package com.slf.reports.repository;

import com.slf.reports.entity.SheetNameEntity;
import org.springframework.data.repository.CrudRepository;

public interface SheetNamesRepository extends CrudRepository<SheetNameEntity, Long> {

    SheetNameEntity findByStream(String sheetName);
}
