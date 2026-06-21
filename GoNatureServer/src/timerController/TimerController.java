package timerController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import serverController.ServerController;

public class TimerController {
	private final long ORDER_REMINDER_INITIAL_DELAY = 0, ORDER_REMINDER_REPEAT_PERIOD = 1,
			WAITING_LIST_INITIAL_DELAY = 0, WAITING_LIST_REPEAT_PERIOD = 1;
	private final int ORDER_REMINDER_EXPIRATION_PERIOD = 2;
	private final String ORDER_STATUS_TO_REMIND = "approved",
							ORDER_STATUS_TO_UPDATE_TO_NO_SHOW = "approved";

	private ScheduledExecutorService scheduler;
	private ServerController serverController;
	private final int threadPoolCount = 5;

	public TimerController(ServerController serverController) {
		this.serverController = serverController;
		scheduler = Executors.newScheduledThreadPool(threadPoolCount);
	}

	
	
	/*
	 * orders need to have only: order id, user id, email, phone, date, hour, status, parkID
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

	private LocalDate getTomorrow() {
		return LocalDate.now().plusDays(1);
	}

	private int getCurrentHourAsInt() {
		return LocalTime.now().getHour();
	}
	
	private int getExpiredHourAsInt() {
		int currentHour = getCurrentHourAsInt();
		return (currentHour-ORDER_REMINDER_EXPIRATION_PERIOD + common.CommonConstants.MAX_HOUR+1)
				% (common.CommonConstants.MAX_HOUR+1);
	}
	
	private LocalDate getExpiredDate() {
		int currentHour = getCurrentHourAsInt();
		if(currentHour - ORDER_REMINDER_EXPIRATION_PERIOD < 0)
			return LocalDate.now();
		return getTomorrow();
	}
}
