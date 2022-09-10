package io.pp.arcade.domain.admin.controller;

import io.pp.arcade.domain.admin.dto.create.PChangeCreateRequestDto;
import io.pp.arcade.domain.admin.dto.delete.PChangeDeleteDto;
import io.pp.arcade.domain.admin.dto.update.PChangeUpdateDto;
import io.pp.arcade.domain.admin.dto.update.PChangeUpdateRequestDto;
import io.pp.arcade.domain.pchange.PChangeService;
import io.pp.arcade.domain.pchange.dto.PChangeDto;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/admin")
public class PChangeAdminControllerImpl implements PChangeAdminController {
    private final PChangeService pChangeService;

    @Override
    @PostMapping(value = "/pChange")
    public void pChangeCreate(PChangeCreateRequestDto createRequestDto, HttpServletRequest request) {
        pChangeService.createPChangeByAdmin(createRequestDto);
    }

    @Override
    @PutMapping(value = "/pChange")
    public void pChangeUpdate(PChangeUpdateRequestDto updateRequestDto, HttpServletRequest request) {
        PChangeUpdateDto updateDto = PChangeUpdateDto.builder()
                .gameId(updateRequestDto.getGameId())
                .userId(updateRequestDto.getUserId())
                .pppChange(updateRequestDto.getPppChange())
                .pppResult(updateRequestDto.getPppResult()).build();
        pChangeService.updatePChangeByAdmin(updateRequestDto.getPchangeId(), updateDto);
    }

    @Override
    @DeleteMapping(value = "/pChange/{pChangeId}")
    public void pChangeDelete(Integer pChangeId, HttpServletRequest request) {
        PChangeDeleteDto deleteDto = PChangeDeleteDto.builder().pChangeId(pChangeId).build();
        pChangeService.deletePChangeByAdmin(deleteDto);
    }

    @Override
    @GetMapping(value = "/pChange/all")
    public List<PChangeDto> pChangeAll(Pageable pageable, HttpServletRequest request) {
        List<PChangeDto> pChanges = pChangeService.findPChangeByAdmin(pageable);
        return pChanges;
    }
}
