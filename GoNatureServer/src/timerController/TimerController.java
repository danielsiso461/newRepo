package timerController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import serverController.ServerController;
/**
 * this class handles scheduled tasks on the server
 */
public class TimerController {
	/**
	 * these variables set the initial delay for the scheduler to launch
	 * and the repeat period (timeUnit will be hours when we launch the schedulers)
	 */
	private final long ORDER_REMINDER_INITIAL_DELAY = 0, ORDER_REMINDER_REPEAT_PERIOD = 1;
	/**
	 * this variable is the time we hold a reminder for
	 */
	private final int ORDER_REMINDER_EXPIRATION_PERIOD = 2;
	/**
	 * these variables are the statuses of the orders to remind 
	 * and to update to no_show
	 */
	private final String ORDER_STATUS_TO_REMIND = "approved",
							ORDER_STATUS_TO_UPDATE_TO_NO_SHOW = "approved";
	/**
	 * this variable is the scheduler service
	 */
	private ScheduledExecutorService scheduler;
	
	/**
	 * this variable is the controller of the server the scheduler is running on
	 */
	private ServerController serverController;
	
	/**
	 * this variable holds the number of threads we deploy schedulers on
	 */
	private final int threadPoolCount = 3;
	
	/**
	 * this is a constructor for the class timerController
	 * @param serverController the controller of the server we run the scheduler on
	 */
	public TimerController(ServerController serverController) {
		this.serverController = serverController;
		scheduler = Executors.newScheduledThreadPool(threadPoolCount);
	}
	
	/**
	 * this method is launched on server launch and starts the reminders schedulers and the no show scheduler
	 */
	public void start() {
		// gathers all orders that need a reminder
		scheduler.scheduleAtFixedRate(() -> {
			try {
				serverController.addReminder(getTomorrow(), getCurrentHourAsInt(), ORDER_STATUS_TO_REMIND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, ORDER_REMINDER_INITIAL_DELAY, ORDER_REMINDER_REPEAT_PERIOD, TimeUnit.HOURS);

		
		// auto cancel
		scheduler.scheduleAtFixedRate(() -> {
			try {
				serverController.removeReminder(getExpiredDate(), getExpiredHourAsInt(), ORDER_STATUS_TO_REMIND);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, ORDER_REMINDER_INITIAL_DELAY, ORDER_REMINDER_REPEAT_PERIOD, TimeUnit.HOURS);
		
		
		//no-show
		scheduler.scheduleAtFixedRate(() -> {
			try {
				serverController.updateNoShows(ORDER_STATUS_TO_UPDATE_TO_NO_SHOW);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, ORDER_REMINDER_INITIAL_DELAY, ORDER_REMINDER_REPEAT_PERIOD, TimeUnit.HOURS);
	}

	/**
	 * this method is used on server shutdown to close all schedulers
	 */
	public void shutDown() {
		serverController.addLog("Shutting down task scheduler, please wait for tasks to finish.");
		scheduler.shutdown();

		try {
			serverController.addLog("Waiting 30 seconds, then forcibly shutting down.");
			scheduler.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			serverController.addLog("Forced scheduler shutdown initiated.");
			scheduler.shutdownNow();
		} catch (Exception e) {
			serverController.addLog("Something went wrong shutting down the scheduler.");
		}
	}

	/**
	 * this method is used to get the date of tomorrow
	 * @return tomorrow's date
	 */
	private LocalDate getTomorrow() {
		return LocalDate.now().plusDays(1);
	}
	
	/**
	 * this method is used to get the current hour as int
	 * @return the current hour as int
	 */
	private int getCurrentHourAsInt() {
		return LocalTime.now().getHour();
	}
	
	/**
	 * this method is used to get the expired hour for auto canceling
	 * @return the expired hour as int
	 */
	public int getExpiredHourAsInt() {
		int currentHour = getCurrentHourAsInt();
		return (currentHour-ORDER_REMINDER_EXPIRATION_PERIOD + common.CommonConstants.MAX_HOUR+1)
				% (common.CommonConstants.MAX_HOUR+1);
	}
	
	/**
	 * this method is used to get the expired date for auto canceling
	 * @return the expired date 
	 */
	public LocalDate getExpiredDate() {
		int currentHour = getCurrentHourAsInt();
		if(currentHour - ORDER_REMINDER_EXPIRATION_PERIOD < 0)
			return LocalDate.now();
		return getTomorrow();
	}
}
