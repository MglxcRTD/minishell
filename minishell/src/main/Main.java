package main;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import tokenizer.Tokenizer;
import tokenizer.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;

public class Main {

	public static void ejecutarComandosForeground(String comando) {

		try {
			TLine linea = Tokenizer.tokenize(comando);
			for (TCommand cmd : linea.getCommands()) {
				List<String> argumentos = cmd.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();
					try {
						int codigodesalida = p.waitFor();
						System.out.println("Proceso finalizado con codigo: " + codigodesalida);

					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static void ejecutarComandosBackground(String comando) {
		try {
			TLine linea = Tokenizer.tokenize(comando);
			for(TCommand cmd : linea.getCommands()) {
				List<String>argumentos = cmd.getArgv();
				ProcessBuilder pb = new ProcessBuilder(argumentos);
				pb.inheritIO();
				try {
					Process p = pb.start();
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		} catch (MissingFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
