package sn.edu.ept.security.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.misc.NotNull;
import sn.edu.ept.security.user.Role;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
//    private String email;
//    private String password;
//    private Role role;
    @NotBlank(message = "Le prénom est obligatoire")
    public String firstname;

    @NotBlank(message = "Le nom est obligatoire")
    public String lastname;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    public String email;

    public String phone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Minimum 6 caractères")
    public String password;

    @NotBlank(message = "Le rôle est obligatoire")
    public Role role;
}
