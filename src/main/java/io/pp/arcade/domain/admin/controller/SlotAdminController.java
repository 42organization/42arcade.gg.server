package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.SlotCreateRequestDto;
import io.pp.arcade.domain.admin.dto.update.SlotGeneratorUpdateDto;
import io.pp.arcade.domain.admin.dto.update.SlotUpdateRequestDto;
import io.pp.arcade.domain.slot.dto.SlotDto;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface SlotAdminController {
    void slotGeneratorRun();
    void slotGeneratorUpdate(@RequestBody SlotGeneratorUpdateDto cron);
    void slotCreate(@RequestBody SlotCreateRequestDto createDto, HttpServletRequest request);
    void slotCreateEmpty(@RequestBody SlotCreateRequestDto createDto, HttpServletRequest request);
    void slotUpdate(@RequestBody SlotUpdateRequestDto updateDto, HttpServletRequest request);
    void slotDelete(@PathVariable Integer id, HttpServletRequest request);
    List<SlotDto> slotAll(Pageable pageable, HttpServletRequest request);
}
