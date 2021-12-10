package uni.fmi.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import uni.fmi.data.entity.LabelEntity;
import uni.fmi.data.entity.TaskListEntity;
import uni.fmi.data.repo.LabelEntityRepo;
import uni.fmi.data.repo.TaskListEntityRepo;

@SpringComponent
public class DataGenerator {

	@Bean
	public CommandLineRunner loadData(TaskListEntityRepo taskListRepo, LabelEntityRepo labelRepo) {
		return args -> {
			Logger logger = LoggerFactory.getLogger(getClass());

			if (taskListRepo.count() == 0L) {

				logger.info("... generating default tasks lists...");

				TaskListEntity todoList = new TaskListEntity("To-Do");
				TaskListEntity inProgressList = new TaskListEntity("In Progress");
				TaskListEntity doneList = new TaskListEntity("Done");

				taskListRepo.save(todoList);
				taskListRepo.save(inProgressList);
				taskListRepo.save(doneList);
			}

			if (labelRepo.count() == 0L) {

				logger.info("... generating default tasks lists...");

				LabelEntity bugfix = new LabelEntity("Bugfix");
				LabelEntity research = new LabelEntity("Research");
				LabelEntity design = new LabelEntity("Design");
				LabelEntity coding = new LabelEntity("Coding");

				labelRepo.save(bugfix);
				labelRepo.save(research);
				labelRepo.save(design);
				labelRepo.save(coding);
			}

			logger.info("Generated demo data");
		};
	}

}