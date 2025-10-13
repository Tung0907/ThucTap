package org.example.thuctap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;

@SpringBootApplication
public class ThuctapApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThuctapApplication.class, args);

		// Chờ server chạy ổn định rồi mở trình duyệt
		new Thread(() -> {
			try {
				Thread.sleep(4000); // chờ 4 giây
				String url = "http://localhost:8080/login.html";
				if (Desktop.isDesktopSupported()) {
					Desktop.getDesktop().browse(new URI(url));
				} else {
					System.out.println("Không hỗ trợ Desktop, hãy mở thủ công: " + url);
				}
			} catch (Exception e) {
				System.out.println("Không thể mở trình duyệt: " + e.getMessage());
			}
		}).start();
	}
}
