package com.thor.controller;

import com.thor.model.Result;
import com.thor.util.AiUtil;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/ai")
public class AiController {
  private final AiUtil aiUtil;
  @PostMapping("/getMessage/{userId}")
  public Result getMessage(@RequestParam("message") String message, @PathVariable long userId){
    return aiUtil.getMessageByAi(userId,message);
  }
}
