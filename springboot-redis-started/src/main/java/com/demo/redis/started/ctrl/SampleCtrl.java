package com.demo.redis.started.ctrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 *
 * @author fuhw/Dean
 * @date 2020-08-05
 */
@RestController
public class SampleCtrl {

    private final ValueOperations<String, String> valueOperations;

    @Autowired
    public SampleCtrl(ValueOperations<String, String> valueOperations) {
        this.valueOperations = valueOperations;
    }

    @GetMapping("/set")
    public void setValue() {
        valueOperations.set("A", "1000", 5, TimeUnit.MINUTES);
    }
}
