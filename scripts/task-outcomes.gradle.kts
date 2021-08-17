
import org.gradle.api.internal.tasks.TaskExecutionOutcome
import org.gradle.api.internal.tasks.TaskStateInternal

class TaskExecutionStatisticsPublisher(logger: Logger) : BuildAdapter(), TaskExecutionListener {

    private val logger: Logger
    private val taskCounts = mutableMapOf<TaskExecutionOutcome, Int>()
    private val statisticsKey = mutableMapOf<TaskExecutionOutcome, String>()

    init {
        this.logger = logger
        TaskExecutionOutcome.values().forEach { outcome ->
            taskCounts[outcome] = 0
        }
        statisticsKey[TaskExecutionOutcome.EXECUTED] = "GradleTasksExecuted"
        statisticsKey[TaskExecutionOutcome.FROM_CACHE] = "GradleTasksFromCache"
        statisticsKey[TaskExecutionOutcome.NO_SOURCE] = "GradleTasksNoSource"
        statisticsKey[TaskExecutionOutcome.SKIPPED] = "GradleTasksSkipped"
        statisticsKey[TaskExecutionOutcome.UP_TO_DATE] = "GradleTasksUpToDate"
    }

    override fun buildFinished(result: BuildResult) {
        taskCounts.forEach { (key, value) ->
            logger.lifecycle("##teamcity[buildStatisticValue key='${statisticsKey[key]}' value='${value}']")
        }
        logger.lifecycle("##teamcity[buildStatisticValue key='GradleTasksTotal' value='${taskCounts.values.sum()}']")
    }

    override fun beforeExecute(task: Task) {}

    override fun afterExecute(task: Task, state: TaskState) {
        val stateInternal = state as TaskStateInternal
        val outcome = stateInternal.outcome as TaskExecutionOutcome
        taskCounts[outcome] = taskCounts[outcome] as Int + 1
    }
}

gradle.addListener(TaskExecutionStatisticsPublisher(logger))
