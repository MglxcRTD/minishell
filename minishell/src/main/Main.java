package main;

import java.io.BufferedReader;
import java.io.File;
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

		try {

			TLine linea = Tokenizer.tokenize(lineacomando);

			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
				return;
			}

			for (TCommand comando : linea.getCommands()) {
				List<String> argumentos = comando.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();

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
					System.err.println("No se pudo imprimir el comando: "
							+ String.join(" ", argumentos + " comprueba si esta bien escrito o si existe"));
				}
			}

		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}

	}

	public static void ejecutarComandosBackground(String lineaentrada) {

		try {

			TLine linea = Tokenizer.tokenize(lineaentrada);

			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
				return;
			}

			for (TCommand cmd : linea.getCommands()) {
				List<String> argumentos = cmd.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();
					System.out.println("[ " + p.pid() + " ]");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("No se pudo imprimir el comando: "
							+ String.join(" ", argumentos + " comprueba si esta bien escrito o si existe"));
				}
			}

		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}

	}

	public static void ejecutarComandosConRedireccion(String comando) {
		try {
			TLine linea = Tokenizer.tokenize(comando);

			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
				return;
			}

			for (TCommand cmd : linea.getCommands()) {
				List<String> argumentos = cmd.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				
				// Entrada estandar "<"

				if (linea.getRedirectInput() != null) {
					pb.redirectInput(new File(linea.getRedirectInput()));
				} else {
					pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
				}

				// salida estandar ">, >>"
				
				if (linea.getRedirectOutput() != null) {
					File archivoSalida = new File(linea.getRedirectOutput());
					if (linea.isAppendOutput()) {
						pb.redirectOutput(ProcessBuilder.Redirect.appendTo(archivoSalida));
					} else {
						pb.redirectOutput(archivoSalida);
					}
				} else {
					pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
				}
				
				//salida de error "2>, 2>>"

				if (linea.getRedirectError() != null) {
					File archivoErrores = new File(linea.getRedirectError());
					if (linea.isAppendError()) {
						pb.redirectError(ProcessBuilder.Redirect.appendTo(archivoErrores));
					} else {
						pb.redirectError(archivoErrores);
					}
				} else {
					pb.redirectError(ProcessBuilder.Redirect.INHERIT);
				}

				try {
					Process p = pb.start();
					p.waitFor();
				} catch (IOException e) {
					System.err.println("No se pudo ejecutar el comando: " + String.join(" ", argumentos));
				} catch (InterruptedException e) {
					System.err.println("Ejecución interrumpida: " + e.getMessage());
				}
			}

		} catch (MissingFileException e) {
			System.err.println(e.getMessage());
		}
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
				break;
			case ("BACKGROUND"):
				ejecutarComandosBackground(comando);
				break;
			case ("REDIRECCION"):
				ejecutarComandosConRedireccion(comando);
				break;
			case ("FOREGROUND"):
				ejecutarComandosForeground(comando);
				break;
			}
		}
	}
}
