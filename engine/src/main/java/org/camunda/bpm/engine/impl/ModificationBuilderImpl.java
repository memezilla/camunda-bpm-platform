package org.camunda.bpm.engine.impl;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cmd.AbstractProcessInstanceModificationCommand;
import org.camunda.bpm.engine.impl.cmd.ActivityAfterInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityBeforeInstantiationCmd;
import org.camunda.bpm.engine.impl.cmd.ActivityCancellationCmd;
import org.camunda.bpm.engine.impl.cmd.ProcessInstanceModificationBatchCmd;
import org.camunda.bpm.engine.impl.cmd.ProcessInstanceModificationCmd;
import org.camunda.bpm.engine.impl.cmd.TransitionInstantiationCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.runtime.ModificationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;

public class ModificationBuilderImpl implements ModificationBuilder {

  protected CommandExecutor commandExecutor;
  protected ProcessInstanceQuery processInstanceQuery;
  protected List<String> processInstanceIds;
  protected List<AbstractProcessInstanceModificationCommand> instructions;
  protected String processDefinitionId;

  protected boolean skipCustomListeners;
  protected boolean skipIoMappings;

  public ModificationBuilderImpl(CommandExecutor commandExecutor, String processDefinitionId) {
    this.commandExecutor = commandExecutor;
    ensureNotNull(NotValidException.class,"processDefinitionId", processDefinitionId);
    this.processDefinitionId = processDefinitionId;
    processInstanceIds = new ArrayList<String>();
    instructions = new ArrayList<AbstractProcessInstanceModificationCommand>();
  }

  @Override
  public ModificationBuilder startBeforeActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    instructions.add(new ActivityBeforeInstantiationCmd(activityId));
    return this;
  }

  @Override
  public ModificationBuilder startAfterActivity(String activityId) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    instructions.add(new ActivityAfterInstantiationCmd(activityId));
    return this;
  }

  @Override
  public ModificationBuilder startTransition(String transitionId) {
    ensureNotNull(NotValidException.class, "transitionId", transitionId);
    instructions.add(new TransitionInstantiationCmd(transitionId));
    return this;
  }

  @Override
  public ModificationBuilder cancelAllForActivity(String activityId) {
    return cancelAllForActivity(activityId, false);
  }

  @Override
  public ModificationBuilder cancelAllForActivity(String activityId, boolean cancelCurrentActiveActivityInstances) {
    ensureNotNull(NotValidException.class, "activityId", activityId);
    ActivityCancellationCmd activityCancellationCmd = new ActivityCancellationCmd(activityId);
    activityCancellationCmd.setCancelCurrentActiveActivityInstances(cancelCurrentActiveActivityInstances);
    instructions.add(activityCancellationCmd);
    return this;
  }

  @Override
  public ModificationBuilder processInstanceIds(List<String> processInstanceIds) {
    this.processInstanceIds = processInstanceIds;
    return this;
  }

  @Override
  public ModificationBuilder processInstanceIds(String... processInstanceIds) {
    if (processInstanceIds == null) {
      this.processInstanceIds = Collections.emptyList();
    }
    else {
      this.processInstanceIds = Arrays.asList(processInstanceIds);
    }
    return this;
  }

  @Override
  public ModificationBuilder processInstanceQuery(ProcessInstanceQuery processInstanceQuery) {
    this.processInstanceQuery = processInstanceQuery;
    return this;
  }

  @Override
  public ModificationBuilder skipCustomListeners() {
    this.skipCustomListeners = true;
    return this;
  }

  @Override
  public ModificationBuilder skipIoMappings() {
    this.skipIoMappings = true;
    return this;
  }

  public void execute(boolean writeUserOperationLog) {
    commandExecutor.execute(new ProcessInstanceModificationCmd(this, writeUserOperationLog));
  }

  @Override
  public void execute() {
    execute(true);
  }

  @Override
  public Batch executeAsync() {
    return commandExecutor.execute(new ProcessInstanceModificationBatchCmd(this));
  }

  public CommandExecutor getCommandExecutor() {
    return commandExecutor;
  }

  public ProcessInstanceQuery getProcessInstanceQuery() {
    return processInstanceQuery;
  }

  public List<String> getProcessInstanceIds() {
    return processInstanceIds;
  }

  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  public void setProcessDefinitionId(String processDefinitionId) {
    this.processDefinitionId = processDefinitionId;
  }

  public List<AbstractProcessInstanceModificationCommand> getInstructions() {
    return instructions;
  }

  public void setInstructions(List<AbstractProcessInstanceModificationCommand> instructions) {
    this.instructions = instructions;
  }

  public boolean isSkipCustomListeners() {
    return skipCustomListeners;
  }

  public boolean isSkipIoMappings() {
    return skipIoMappings;
  }

}
