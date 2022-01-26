package com.henrique.virtualteacher.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CourseModel {

    @NotBlank
    private String title;

    @NotBlank
    private EnumTopics topic;

    @NotBlank
    private String description;

    @NotBlank
    private int difficulty;

    @NotBlank
    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private LocalDate startingDate;

    //enabled not necessary
}
