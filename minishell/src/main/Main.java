package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.List;
import java.util.Scanner;

import tokenizer.Tokenizer;
import tokenizer.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;

public class Main {

	public static void ejecutarComandosForeground(String lineacomando) {

		if (lineacomando == null || lineacomando.trim().isEmpty()) {
			System.err.println("Error: comando vacío o inválido.");
			return;
		}

		try {

			TLine linea = Tokenizer.tokenize(lineacomando);

			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
			}

			for (TCommand comando : linea.getCommands()) {
				List<String> argumentos = comando.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();
					try (BufferedReader salida = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
						String lineasalida;
						while ((lineasalida = salida.readLine()) != null) {
							System.out.println(lineasalida);
						}
					}

					try (BufferedReader errores = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
						String lineaerrores;
						while ((lineaerrores = errores.readLine()) != null) {
							System.err.println(lineaerrores);
						}
					}

					try {
						int codigosalida = p.waitFor();
						if (codigosalida != 0) {
							System.err.println("El proceso ha terminado con error: " + codigosalida);
						} else {
							System.out.println("Proceso terminado correctamente, salida: " + codigosalida);
						}

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.err.println(e.getMessage());
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("No se pudo imprimir el comando: " + String.join(" ", argumentos));
				}
			}

		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}

	}

	public static void ejecutarComandosBackground(String lineaentrada) {

		if (lineaentrada == null || lineaentrada.trim().isEmpty()) {
			System.err.println("Error: comando vacío o inválido.");
			return;
		}

		try {

			TLine linea = Tokenizer.tokenize(lineaentrada);

			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
			}

			for (TCommand cmd : linea.getCommands()) {
				List<String> argumentos = cmd.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();
					try (BufferedReader salida = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
						String lineasalida;
						while ((lineasalida = salida.readLine()) != null) {
							System.out.println(lineasalida);
						}
					}

					try (BufferedReader errores = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
						String lineaerrores;
						while ((lineaerrores = errores.readLine()) != null) {
							System.err.println(lineaerrores);
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("No se pudo imprimir el comando: " + String.join(" ", argumentos));
				}
			}

		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}

	}

	public static void ejecutarComandosConRedireccion(String comando) {

	}

	public static String clasificarComandos(String comando) {
		if (comando.contains("|")) {
			return "PIPE";
		}

		else if (comando.endsWith("&")) {
			return "BACKGROUND";
		}

		else if (comando.contains(">") || comando.contains("<")) {
			return "REDIRECCION";
		} else {
			return "FOREGROUND";
		}

	}

	public static void main(String[] args) {

		Scanner sc = new Scanner(System.in);

		System.out.println("Introduce el nombre que quieres que salga en la shell");

		String nombre = sc.nextLine();

		if (nombre.isEmpty()) {
			nombre = "default";
		}

		String comando = "";
		while (true) {
			System.out.print("\u001B[32m" + nombre + "@>\u001B[0m ");
			comando = sc.nextLine();
			if (comando.equalsIgnoreCase("exit")) {
				System.out.println("Saliendo del minishell, ¡hasta la proxima!");
				break;
			}
			switch (clasificarComandos(comando)) {
			case ("PIPE"):
				System.out.println("Me falta hacer pipes");
			case ("BACKGROUND"):
				ejecutarComandosBackground(comando);
			case ("REDIRECCION"):
				System.out.println("Me falta redireccion");
			case ("FOREGROUND"):
				ejecutarComandosForeground(comando);
			}
		}
	}
}
