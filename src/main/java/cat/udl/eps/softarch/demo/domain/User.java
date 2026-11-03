package cat.udl.eps.softarch.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;

@Entity
@Table(name = "PickCarUser") //Avoid collision with system table User
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class User extends UriEntity<Long> implements UserDetails {

	public enum Role {
		DRIVER, COMPANY_STAFF, ADMIN
	}

	public enum Status {
		PENDING_EMAIL, ACTIVE
	}

	public static PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Email
	@Column(unique = true)
	private String email;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@NotBlank
	@Length(min = 8, max = 256)
	private String password;

	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private boolean passwordReset;

	private String fullName;

	private String phone;

	@Enumerated(EnumType.STRING)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Role role = Role.DRIVER;

	@Enumerated(EnumType.STRING)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Status status = Status.PENDING_EMAIL;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private ZonedDateTime emailVerifiedAt;

	@CreatedDate
	@DateTimeFormat
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Instant created;

	public void encodePassword() {
		this.password = passwordEncoder.encode(this.password);
	}

	@Override
	@JsonIgnore
	public String getUsername() { return email; }

	@Override
	@JsonValue(value = false)
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return AuthorityUtils.createAuthorityList("ROLE_" + role.name());
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
}
