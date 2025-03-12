package com.master.controller;

import com.master.constant.MasterConstant;
import com.master.dto.ApiMessageDto;
import com.master.dto.ErrorCode;
import com.master.dto.ResponseListDto;
import com.master.dto.tag.TagDto;
import com.master.exception.BadRequestException;
import com.master.form.tag.CreateTagForm;
import com.master.form.tag.UpdateTagForm;
import com.master.mapper.TagMapper;
import com.master.model.Tag;
import com.master.model.criteria.TagCriteria;
import com.master.repository.LocationRepository;
import com.master.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/tag")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class TagController extends ABasicController {
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagMapper tagMapper;
    @Autowired
    private LocationRepository locationRepository;

    @GetMapping(value = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TA_V')")
    public ApiMessageDto<TagDto> get(@PathVariable("id") Long id) {
        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag == null) {
            throw new BadRequestException(ErrorCode.TAG_ERROR_NOT_FOUND, "Not found tag");
        }
        return makeSuccessResponse(tagMapper.fromEntityToTagDto(tag), "Get tag success");
    }

    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TA_L')")
    public ApiMessageDto<ResponseListDto<List<TagDto>>> list(TagCriteria tagCriteria, Pageable pageable) {
        if (MasterConstant.BOOLEAN_FALSE.equals(tagCriteria.getIsPaged())) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdDate").descending());
        } else {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
        }
        Page<Tag> listTag = tagRepository.findAll(tagCriteria.getCriteria(), pageable);
        ResponseListDto<List<TagDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(tagMapper.fromEntityListToTagDtoList(listTag.getContent()));
        responseListObj.setTotalPages(listTag.getTotalPages());
        responseListObj.setTotalElements(listTag.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list tag success");
    }

    @GetMapping(value = "/auto-complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiMessageDto<ResponseListDto<List<TagDto>>> autoComplete(TagCriteria tagCriteria, @PageableDefault Pageable pageable) {
        if (MasterConstant.BOOLEAN_FALSE.equals(tagCriteria.getIsPaged())) {
            pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("createdDate").descending());
        } else {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
        }
        tagCriteria.setStatus(MasterConstant.STATUS_ACTIVE);
        Page<Tag> listTag = tagRepository.findAll(tagCriteria.getCriteria(), pageable);
        ResponseListDto<List<TagDto>> responseListObj = new ResponseListDto<>();
        responseListObj.setContent(tagMapper.fromEntityListToTagDtoListAutoComplete(listTag.getContent()));
        responseListObj.setTotalPages(listTag.getTotalPages());
        responseListObj.setTotalElements(listTag.getTotalElements());
        return makeSuccessResponse(responseListObj, "Get list auto-complete tag success");
    }

    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TA_C')")
    public ApiMessageDto<String> create(@Valid @RequestBody CreateTagForm form, BindingResult bindingResult) {
        if (tagRepository.existsByColor(form.getColor())) {
            throw new BadRequestException(ErrorCode.TAG_ERROR_COLOR_EXISTED, "Tag color existed");
        }
        Tag tag = tagMapper.fromCreateTagFormToEntity(form);
        tagRepository.save(tag);
        return makeSuccessResponse(null, "Create tag success");
    }

    @PutMapping(value = "/update", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TA_U')")
    public ApiMessageDto<String> update(@Valid @RequestBody UpdateTagForm form, BindingResult bindingResult) {
        Tag tag = tagRepository.findById(form.getId()).orElse(null);
        if (tag == null) {
            throw new BadRequestException(ErrorCode.TAG_ERROR_NOT_FOUND, "Not found tag");
        }
        if (tagRepository.existsByColorAndIdNot(form.getColor(), tag.getId())) {
            throw new BadRequestException(ErrorCode.TAG_ERROR_COLOR_EXISTED, "Tag color existed");
        }
        tagMapper.fromUpdateTagFormToEntity(form, tag);
        tagRepository.save(tag);
        return makeSuccessResponse(null, "Update tag success");
    }

    @DeleteMapping(value = "/delete/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('TA_D')")
    public ApiMessageDto<String> delete(@PathVariable("id") Long id) {
        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag == null) {
            throw new BadRequestException(ErrorCode.TAG_ERROR_NOT_FOUND, "Not found tag");
        }
        locationRepository.updateAllTagIdToNullByTagId(id);
        tagRepository.deleteById(id);
        return makeSuccessResponse(null, "Delete tag success");
    }
}
