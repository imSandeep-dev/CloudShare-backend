package com.sandeep.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_credits")
public class UserCredits {

    @Id
    private String id;
    private String clerkId;
    private Integer credits;
    private String plan;

}
