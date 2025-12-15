package com.atlas.user.entity;

import com.atlas.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * User address entity for shipping and billing.
 */
@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AddressType type = AddressType.SHIPPING;

    @Column(name = "recipient_name")
    private String recipientName;

    @Column(name = "phone_number")
    private String phoneNumber;

    public enum AddressType {
        SHIPPING,
        BILLING,
        BOTH
    }

    public String getFormattedAddress() {
        return String.format("%s, %s, %s %s, %s", street, city, state, postalCode, country);
    }
}
