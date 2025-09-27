package net.cycastic.sigil.controller;

import an.awesome.pipelinr.Pipelinr;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.application.partition.create.CreatePartitionCommand;
import net.cycastic.sigil.application.partition.delete.DeletePartitionCommand;
import net.cycastic.sigil.application.partition.get.GetPartitionCommand;
import net.cycastic.sigil.application.partition.member.add.AddPartitionMemberCommand;
import net.cycastic.sigil.application.partition.member.get.GetPartitionMemberCommand;
import net.cycastic.sigil.application.partition.member.modify.ModifyPartitionMemberCommand;
import net.cycastic.sigil.application.partition.member.query.QueryPartitionMemberCommand;
import net.cycastic.sigil.application.partition.member.remove.RemovePartitionMemberCommand;
import net.cycastic.sigil.application.partition.member.self.GetSelfPartitionUserCommand;
import net.cycastic.sigil.application.partition.query.QueryPartitionSingleLevelCommand;
import net.cycastic.sigil.controller.annotation.RequirePartitionId;
import net.cycastic.sigil.controller.annotation.RequireTenantId;
import net.cycastic.sigil.domain.dto.FolderItemDto;
import net.cycastic.sigil.domain.dto.IdDto;
import net.cycastic.sigil.domain.dto.listing.PartitionDto;
import net.cycastic.sigil.domain.dto.PartitionUserDto;
import net.cycastic.sigil.domain.dto.paging.PageResponseDto;
import org.springframework.web.bind.annotation.*;

@RestController
@RequireTenantId
@RequiredArgsConstructor
@RequestMapping("api/partitions")
public class PartitionsController {
    private final Pipelinr pipelinr;

    @GetMapping("partition")
    public PartitionDto getPartition(GetPartitionCommand command){
        return pipelinr.send(command);
    }

    @GetMapping
    public PageResponseDto<FolderItemDto> query(QueryPartitionSingleLevelCommand command){
        return pipelinr.send(command);
    }

    @PostMapping
    public IdDto create(@Valid @RequestBody CreatePartitionCommand command){
        return pipelinr.send(command);
    }

    @DeleteMapping
    @RequirePartitionId
    public void delete(){
        pipelinr.send(DeletePartitionCommand.INSTANCE);
    }

    @PostMapping("members")
    @RequirePartitionId
    public void addMember(@RequestBody AddPartitionMemberCommand command){
        pipelinr.send(command);
    }

    @PatchMapping("members")
    @RequirePartitionId
    public void modifyMember(@RequestBody ModifyPartitionMemberCommand command){
        pipelinr.send(command);
    }

    @DeleteMapping("members")
    @RequirePartitionId
    public void removeMember(RemovePartitionMemberCommand command){
        pipelinr.send(command);
    }

    @GetMapping("members")
    @RequirePartitionId
    public PageResponseDto<PartitionUserDto> queryMembers(QueryPartitionMemberCommand command){
        return pipelinr.send(command);
    }

    @GetMapping("members/self")
    @RequirePartitionId
    public PartitionUserDto getSelf(){
        return pipelinr.send(GetSelfPartitionUserCommand.INSTANCE);
    }

    @GetMapping("members/member")
    @RequirePartitionId
    public PartitionUserDto getMember(GetPartitionMemberCommand command){
        return pipelinr.send(command);
    }
}
