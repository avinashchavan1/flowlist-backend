package com.flowlist.controller;

import com.flowlist.dto.*;
import com.flowlist.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getAll(@AuthenticationPrincipal UserDetails u) {
        return taskService.getAll(u.getUsername());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(@AuthenticationPrincipal UserDetails u,
                               @Valid @RequestBody TaskRequest req) {
        return taskService.create(u.getUsername(), req);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@AuthenticationPrincipal UserDetails u,
                               @PathVariable String id,
                               @Valid @RequestBody TaskRequest req) {
        return taskService.update(u.getUsername(), id, req);
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse complete(@AuthenticationPrincipal UserDetails u,
                                 @PathVariable String id) {
        return taskService.complete(u.getUsername(), id);
    }

    @PatchMapping("/{id}/uncomplete")
    public TaskResponse uncomplete(@AuthenticationPrincipal UserDetails u,
                                   @PathVariable String id) {
        return taskService.uncomplete(u.getUsername(), id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UserDetails u,
                       @PathVariable String id) {
        taskService.delete(u.getUsername(), id);
    }
}
