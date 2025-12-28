package com.learnplatform.web;

import com.learnplatform.entity.Tag;
import com.learnplatform.service.TagService;
import com.learnplatform.web.dto.TagCreateRequest;
import com.learnplatform.web.dto.TagPatchRequest;
import com.learnplatform.web.dto.TagView;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tags")
@Validated
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping
    @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
    public TagView create(@Valid @RequestBody TagCreateRequest req) {
        Tag tag = tagService.create(req.name());
        return toView(tag);
    }

    @GetMapping
    public List<TagView> list() {
        return tagService.list().stream().map(TagController::toView).toList();
    }

    @GetMapping("/{id}")
    public TagView get(@PathVariable @Positive Long id) {
        return toView(tagService.getOrThrow(id));
    }

    @PatchMapping("/{id}")
    public TagView patch(@PathVariable @Positive Long id, @Valid @RequestBody TagPatchRequest req) {
        return toView(tagService.update(id, req.name()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        tagService.delete(id);
    }

    private static TagView toView(Tag tag) {
        return new TagView(tag.getId(), tag.getName());
    }
}
