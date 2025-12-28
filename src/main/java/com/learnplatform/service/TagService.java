package com.learnplatform.service;

import com.learnplatform.entity.Tag;
import com.learnplatform.exception.BadRequestException;
import com.learnplatform.exception.ConflictException;
import com.learnplatform.exception.NotFoundException;
import com.learnplatform.repository.TagRepository;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag create(String name) {
        String normalized = normalize(name);
        if (tagRepository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Tag already exists: name=" + normalized);
        }
        return tagRepository.save(new Tag(normalized));
    }

    @Transactional(readOnly = true)
    public Tag getOrThrow(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag not found: id=" + id));
    }

    @Transactional(readOnly = true)
    public List<Tag> list() {
        return tagRepository.findAll();
    }

    @Transactional
    public Tag update(Long id, String newName) {
        Tag tag = getOrThrow(id);
        String normalized = normalize(newName);
        if (!tag.getName().equalsIgnoreCase(normalized) && tagRepository.existsByNameIgnoreCase(normalized)) {
            throw new ConflictException("Tag already exists: name=" + normalized);
        }
        tag.setName(normalized);
        return tagRepository.save(tag);
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = getOrThrow(id);
        if (tagRepository.isUsedByAnyCourse(id)) {
            throw new ConflictException("Cannot delete tag id=" + id + " because it is used by courses");
        }
        tagRepository.delete(tag);
    }

    private static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Tag name must not be blank");
        }
        return raw.trim();
    }
}
