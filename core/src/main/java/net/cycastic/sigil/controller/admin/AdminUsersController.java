package net.cycastic.sigil.controller.admin;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.admin.user.create.ForceCreateUserCommand;
import net.cycastic.sigil.application.admin.user.get.GetUserByIdCommand;
import net.cycastic.sigil.application.admin.user.list.ListUsersCommand;
import net.cycastic.sigil.application.admin.user.update.details.UpdateUserDetailsCommand;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.UserDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/admin/users")
public class AdminUsersController {
    private final Pipelinr pipelinr;

    @PutMapping
    public IdDto createUser(@Valid @RequestBody ForceCreateUserCommand command){
        return pipelinr.send(command);
    }

    @GetMapping
    public PageResponseDto<UserDto> listUsers(@Valid ListUsersCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("user")
    public UserDto getUser(@Valid GetUserByIdCommand command){
        return pipelinr.send(command);
    }

    @PostMapping
    public void updateUserDetails(@Valid @RequestBody UpdateUserDetailsCommand command){
        pipelinr.send(command);
    }
}
