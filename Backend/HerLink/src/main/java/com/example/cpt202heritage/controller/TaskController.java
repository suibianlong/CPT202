package com.example.cpt202heritage.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;



// Annotation to indicate that class is a REST
@RestController
public class TaskController {

    private String taskName = "";

    // www.mytask.cn/tasks/count
    // localhost:8080/tasks/count
    @GetMapping("/tasks/count")
    public int numberofTasks(){
        return 10;
    }
    

    @PostMapping("/savetask")
    public void saveTask(){
        taskName = "Name";
    }
}
