package net.minecraft.entity.ai;

import com.google.common.collect.Lists;
import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Iterator;
import java.util.List;

public class EntityAITasks {
    private static final Logger logger = LogManager.getLogger();
    private final List<EntityAITasks.EntityAITaskEntry> taskEntries = Lists.newArrayList();
    private final List<EntityAITasks.EntityAITaskEntry> executingTaskEntries = Lists.newArrayList();

    /**
     * Instance of Profiler.
     */
    private final Profiler theProfiler;
    private final int tickRate = 3;
    private int tickCount;

    public EntityAITasks(Profiler profilerIn) {
        theProfiler = profilerIn;
    }

    /**
     * Add a now AITask. Args : priority, task
     */
    public void addTask(int priority, EntityAIBase task) {
        taskEntries.add(new EntityAITasks.EntityAITaskEntry(priority, task));
    }

    /**
     * removes the indicated task from the entity's AI tasks.
     */
    public void removeTask(EntityAIBase task) {
        Iterator<EntityAITasks.EntityAITaskEntry> iterator = taskEntries.iterator();

        while (iterator.hasNext()) {
            EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry = iterator.next();
            EntityAIBase entityaibase = entityaitasks$entityaitaskentry.action;

            if (entityaibase == task) {
                if (executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
                    entityaibase.resetTask();
                    executingTaskEntries.remove(entityaitasks$entityaitaskentry);
                }

                iterator.remove();
            }
        }
    }

    public void onUpdateTasks() {
        theProfiler.startSection("goalSetup");

        if (tickCount++ % tickRate == 0) {
            Iterator iterator = taskEntries.iterator();
            label38:

            while (true) {
                EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry;

                while (true) {
                    if (!iterator.hasNext()) {
                        break label38;
                    }

                    entityaitasks$entityaitaskentry = (EntityAITasks.EntityAITaskEntry) iterator.next();
                    boolean flag = executingTaskEntries.contains(entityaitasks$entityaitaskentry);

                    if (!flag) {
                        break;
                    }

                    if (!canUse(entityaitasks$entityaitaskentry) || !canContinue(entityaitasks$entityaitaskentry)) {
                        entityaitasks$entityaitaskentry.action.resetTask();
                        executingTaskEntries.remove(entityaitasks$entityaitaskentry);
                        break;
                    }
                }

                if (canUse(entityaitasks$entityaitaskentry) && entityaitasks$entityaitaskentry.action.shouldExecute()) {
                    entityaitasks$entityaitaskentry.action.startExecuting();
                    executingTaskEntries.add(entityaitasks$entityaitaskentry);
                }
            }
        } else {
            Iterator<EntityAITasks.EntityAITaskEntry> iterator1 = executingTaskEntries.iterator();

            while (iterator1.hasNext()) {
                EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry1 = iterator1.next();

                if (!canContinue(entityaitasks$entityaitaskentry1)) {
                    entityaitasks$entityaitaskentry1.action.resetTask();
                    iterator1.remove();
                }
            }
        }

        theProfiler.endSection();
        theProfiler.startSection("goalTick");

        for (EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry2 : executingTaskEntries) {
            entityaitasks$entityaitaskentry2.action.updateTask();
        }

        theProfiler.endSection();
    }

    /**
     * Determine if a specific AI Task should continue being executed.
     */
    private boolean canContinue(EntityAITasks.EntityAITaskEntry taskEntry) {
        boolean flag = taskEntry.action.continueExecuting();
        return flag;
    }

    /**
     * Determine if a specific AI Task can be executed, which means that all running higher (= lower int value) priority
     * tasks are compatible with it or all lower priority tasks can be interrupted.
     */
    private boolean canUse(EntityAITasks.EntityAITaskEntry taskEntry) {
        for (EntityAITasks.EntityAITaskEntry entityaitasks$entityaitaskentry : taskEntries) {
            if (entityaitasks$entityaitaskentry != taskEntry) {
                if (taskEntry.priority >= entityaitasks$entityaitaskentry.priority) {
                    if (!areTasksCompatible(taskEntry, entityaitasks$entityaitaskentry) && executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
                        return false;
                    }
                } else if (!entityaitasks$entityaitaskentry.action.isInterruptible() && executingTaskEntries.contains(entityaitasks$entityaitaskentry)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Returns whether two EntityAITaskEntries can be executed concurrently
     */
    private boolean areTasksCompatible(EntityAITasks.EntityAITaskEntry taskEntry1, EntityAITasks.EntityAITaskEntry taskEntry2) {
        return (taskEntry1.action.getMutexBits() & taskEntry2.action.getMutexBits()) == 0;
    }

    class EntityAITaskEntry {
        public EntityAIBase action;
        public int priority;

        public EntityAITaskEntry(int priorityIn, EntityAIBase task) {
            priority = priorityIn;
            action = task;
        }
    }
}
