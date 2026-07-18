package com.dogukantez.soaptask.model;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VatValidationResult {  //soaptan dönen bilgileri temsil eden entity-model

    private boolean valid;  //soap ile aynı isim olması için isValid demedim.

    private String countryCode;
    private String vatNumber;

    private String name;
    private String address;

    private LocalDate requestDate;
}
