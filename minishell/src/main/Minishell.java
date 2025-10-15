package main;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import tokenizer.MissingFileException;
import tokenizer.TCommand;
import tokenizer.TLine;
import tokenizer.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class Minishell {

	/*
	 * Lo primero que vamos a hacer es declararnos una variable estática de nuestro
	 * directorio actual para poder modificarlo mas adelante con el comando cd
	 */

	private static File currentDir = new File(System.getProperty("user.dir"));

	/*
	 * Creamos el metodo para ejecutar la linea de comandos que vayamos a recibir.
	 */

	public static void ejecutarLinea(String lineaComando) {

		/*
		 * Lo primero que hacemos aqui es crear un objeto TLlinea que "tokenice" los
		 * comandos u argumentos a traves de la clase Tokenize. Además tambien
		 * aprovechamos para comprobar si la linea de comandos esta vacia o si el
		 * comando no existe lanzar un mensaje de error. El return final es para que si
		 * se diera el error, la minishell pueda seguir recibiendo comandos sin quedarse
		 * bloqueada.
		 */

		try {
			TLine linea = Tokenizer.tokenize(lineaComando);
			if (linea == null || linea.getCommands() == null || linea.getCommands().isEmpty()) {
				System.err.println("Error: comando vacío o inválido.");
				return;
			}

			/*
			 * ^ Aquí guardamos los comandos de la linea Tline en una Lista de TCommands,
			 * sobretodo para saber si vamos a trabajar con pipes o vamos a ejecutar
			 * comandos simples.
			 */

			List<TCommand> comandos = linea.getCommands();

			/*
			 * Como bien hemos dicho antes, si la lista contiene mas de un comando significa
			 * que si o si vamos a ejecutar una pipe, sino vamos a comprobar si el comando
			 * empieza por cd, de ser así vamos a modificar nuestro directorio actual
			 * ejecutando el comando cd, sino pues ejecutamos un comando simple
			 */

			if (comandos.size() > 1) {
				ejecutarPipes(linea, linea.isBackground(), currentDir);
			} else {
				if (comandos.get(0).getFilename().equals("cd")) {
					currentDir = ejecutarcd(linea, currentDir);
				} else {
					ejecutarComandoSimple(linea, linea.isBackground(), currentDir);
				}
			}

		} catch (MissingFileException e) {
			System.err.println("archivo no encontrado: " + e.getMessage());
		}
	}

	/*
	 * Este es el comando para ejecutar Pipes, para ello recibiremos un objeto Tline
	 * y la variable booleana background para saber si alguno de los comandos que se
	 * ejecuten durante la pipe se ejecuta en background
	 */

	private static void ejecutarPipes(TLine linea, boolean background, File currentdir) {

		/*
		 * Vamos a crear dos listas, uno que reciba todos los comandos que lleve la
		 * linea y otro para almacenar todos los constructores (comandos + argumentos)
		 * que se necesiten para poder inicializar la pipe
		 */

		List<ProcessBuilder> constructores = new ArrayList<>();
		List<TCommand> comandos = linea.getCommands();

		/*
		 * Recorremos la lista de comandos y por cada uno de ellos creamos un
		 * constructor (ProcessBuilder)
		 */

		for (int i = 0; i < comandos.size(); i++) {
			TCommand cmd = comandos.get(i);
			ProcessBuilder pb = new ProcessBuilder(cmd.getArgv());
			
			/*
			 Aqui recordamos al Processbuilder que queremos que ejecute el comando
			 en nuestro directorio actual.
			 */
			
			pb.directory(currentdir);

			/*
			 * Aqui estamos redirigiendo la salida el ultimo elemento de la pipe utilizando
			 * el metodo aplicarRedirecciones en el caso de que hubiera "<, >, >>, 2> o 2>>"
			 */

			if (i == comandos.size() - 1) {
				aplicarRedirecciones(pb, linea);
			}

			/*
			 * Añadimos los constructores creados a la lista de constructores anteriormente
			 * creada
			 */

			constructores.add(pb);
		}

		try {

			/*
			 * Inicializamos la Pipe creado una lista de procesos para poder ejecutar dichos
			 * procesos. Además sacamos el proceso último para poder trabajar con sus
			 * salidas por consola.
			 */

			List<Process> procesos = ProcessBuilder.startPipeline(constructores);
			Process ultimo = procesos.get(procesos.size() - 1);

			/*
			 * Si es background el proceso sacaremos el PID por consola.
			 */

			if (background) {
				System.out.println("[Proceso en background: PID " + ultimo.pid() + "]");
			} else {

				/*
				 * Aqui vamos a crear 2 salidas para el último comando para que pueda salir por
				 * consola independientemente de si funciona el ultimo comando o en caso de que
				 * sea un error salga por la salida de errores
				 */

				try (BufferedReader salida = new BufferedReader(new InputStreamReader(ultimo.getInputStream()));
						BufferedReader salida_errores = new BufferedReader(
								new InputStreamReader(ultimo.getErrorStream()))) {

					String linea_salida;
					while ((linea_salida = salida.readLine()) != null) {
						System.out.println(linea_salida);
					}

					String linea_salida_errores;
					while ((linea_salida_errores = salida_errores.readLine()) != null) {
						System.err.println(linea_salida_errores);
					}

					/*
					 * Aqui estamos tratando el proceso como si fuese en foreground, aplicacion un
					 * waitfor para que el proceso se ejecute y no se pueda ingresar otro hasta que
					 * este proceso hay terminado. También imprimimos el código para saber si ha
					 * terminado bien y se ha ejecutado bien o ha saltado algún error
					 */

					int codigo = ultimo.waitFor();
					if (codigo != 0) {
						System.err.println("Pipeline finalizó con error: " + codigo);
					} else {
						System.out.println("Pipeline finalizado con exito, salida: " + codigo);
					}

					/*
					 * En estos catch de aqui estamos capturando si en algún momento la ejecucion ha
					 * sido interrumpida o si ha ocurrido algun error durante la ejecución del
					 * pipeline
					 */

				} catch (InterruptedException e) {
					System.err.println("Ejecución interrumpida: " + e.getMessage());
				}
			}

		} catch (IOException e) {
			System.err.println("Error ejecutando pipeline: " + e.getMessage());
		}
	}

	/*
	 * Este método va a servir para ejecutar comandos simples, es decir, que no
	 * tengan ninguna pipe y solo sea un comando con argumentos. Para ello
	 * recibiremos un objeto de la clase Tline con el comando y sus argumentos y una
	 * variable booleana para ver si el comando va a ser ejecutado en foreground o
	 * en background
	 */

	private static void ejecutarComandoSimple(TLine linea, boolean background, File currentdir) {

		/*
		 * Lo primero que vamos a hacer es sacar el comando del objeto Tline, para ello
		 * crearemos un objeto de la clase TCommand y utilizaremos el getter getCommands
		 * de la clase TLine para obtener la lista que tiene el comando y sus argumentos
		 * y sacaremos el primer elemento de la lista para obtener el comando. Después
		 * creamos el constructor utilizando la variable coman2 y usando el getter
		 * getArgv para obtener asi tambien sus argumentos.
		 */

		TCommand coman2 = linea.getCommands().getFirst();
		ProcessBuilder pb = new ProcessBuilder(coman2.getArgv());
		pb.directory(currentdir);

		/*
		 * En el caso de que hubiera redirecciones como: "<, >, >>, 2>, 2>>" las
		 * aplicamos.
		 */

		aplicarRedirecciones(pb, linea);

		/*
		 * Aquí al igual que hicimos en el metodo de las pipes, asignamos las salidas para que salgan
		 * por consola.
		 */

		Process p;
		try {
			p = pb.start();
			try (BufferedReader salida = new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader errores = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

				String lineaSalida;
				try {
					while ((lineaSalida = salida.readLine()) != null) {
						System.out.println(lineaSalida);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String lineaError;
				try {
					while ((lineaError = errores.readLine()) != null) {
						System.err.println(lineaError);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				/*
				 Comprobamos si se va a ejecutar en background o no, en el caso
				 de no ser asi hacemos un waitfor para que hasta que no se ejecute
				 el proceso la minishell no ejecutará mas comandos, si es background si
				 permite el ingreso de nuevos y ademas muestra el PID.
				 */

				if (!background) {
					int codigo = p.waitFor();
					if (codigo != 0) {
						System.err.println("El proceso terminó con error: " + codigo);
					} else {
						System.out.println("Proceso terminado correctamente con código: " + codigo);
					}
				} else {
					System.out.println("[Proceso en background: PID " + p.pid() + "]");
				}

			} catch (InterruptedException e) {
				System.err.println("Ejecución interrumpida: " + e.getMessage());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				System.err.println(
						"Fallo al ejecutar el comando, comprueba si se ha ingresado bien o esta en el Sistema Operativo correcto");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(
					"Fallo al ejecutar el comando, comprueba si se ha ingresado bien o esta en el Sistema Operativo correcto");
		}

	}

	/*
	 * En este método es donde trabajaremos las redirecciones en caso de que se
	 * desee realizar redirecciones a archivos. Para ello recibiremos un objeto de
	 * la clase ProcessBuilder para poder trabajar con los comandos de redireccion y
	 * TLine para saber con que tokens estamos trabajando.
	 */

	private static void aplicarRedirecciones(ProcessBuilder pb, TLine linea) {

		/*
		 * Aqui estamos tratando el comando de entrada "<" y en caso de que no exista 
		 * lanzamos un mensaje de error.
		 */

		if (linea.getRedirectInput() != null) {
			File input = new File(linea.getRedirectInput());
			if (!input.exists()) {
				System.err.println("Archivo de entrada no encontrado");
			} else {
				pb.redirectInput(input);
			}

		}

		/*
		 * Aqui estamos tratando el comando de salida, primero comprobando que no sea
		 * nulo. En el caso de que sea ">>" para no sobreescribir utilizaremos el
		 * appendTo, en caso de que sea solo ">" utilizaremos unicamente el
		 * redirectOutput. En caso de que no exista lanzamos un mensaje de error.
		 */

		if (linea.getRedirectOutput() != null) {
			File salida = new File(linea.getRedirectOutput());
			if (!salida.exists()) {
				System.err.println("Archivo de salida no encontrado");
			}
			if (linea.isAppendOutput()) {
				pb.redirectOutput(ProcessBuilder.Redirect.appendTo(salida));
			} else {
				pb.redirectOutput(salida);
			}
		}

		/*
		 * Aqui estamos tratando la salida de errores donde se nos escribirá en un
		 * documento el por que esta fallando un proceso. En caso de que la salida sea
		 * "2>>" para no sobreescribir el archivo usaremos el appendTo en el caso de que
		 * sea "2>" utilizaremos redirectError simplemente. En caso de que no exista el archivo
		 * lanzamos un mensaje de error.
		 */

		if (linea.getRedirectError() != null) {
			File salida_errores = new File(linea.getRedirectError());
			if(!salida_errores.exists()) {
				System.err.println("Archivo de salida de errores no encontrado");
			}
			if (linea.isAppendError()) {
				pb.redirectError(ProcessBuilder.Redirect.appendTo(salida_errores));
			} else {
				pb.redirectError(salida_errores);
			}
		}
	}

	/*
	 * Método para cambiar el directorio. Para ello necesitamos la linea del comando con
	 * sus argumentos y nuestro directorio actual.
	 */

	public static File ejecutarcd(TLine linea, File currentDir) {
		
		/*
		 Lo primero que vamos a hacer es separar el comando cd de la ruta a la que queremos
		 cambiar que en este caso esta en los argumentos.
		 */
		
		
		TCommand cmd = linea.getCommands().get(0);
		List<String> args = cmd.getArgv();
		
		/*
		 Aqui vemos que si el unico argumento es el comando, es decir "cd" lo utilizaremos
		 para volver al path home
		 */

		if (args.size() == 1) {
			File home = new File(System.getProperty("user.home"));
			System.out.println("Directorio cambiado a: " + home.getAbsolutePath());
			return home;
		}
		
		/*
		 Aqui estamos sacando el Path al que queremos cambiar el directorio
		 */

		File destino = new File(args.get(1));
		
		/*
		 En el caso de que nuestra ruta sea relativa, vamos a coger nuestra direccion
		 actual y la vamos a concatenar con la ruta relativa proporcionada
		 */
		
		if (!destino.isAbsolute()) {
			destino = new File(currentDir, args.get(1));
		}
		
		/*
		 En el caso de que el directorio no exista o no sera un directorio lanzamos 
		 un mensaje de error y devolvemos nuestro directorio actual.
		 */

		if (!destino.exists() || !destino.isDirectory()) {
			System.err.println("cd: no existe el directorio: " + args.get(1));
			return currentDir;
		}
		
		/*
		 En el caso de que todo haya ido bien lanzamos un mensaje para comprobar
		 el exito de nuestro cambio de Directorio y devolvemos el nuevo directorio.
		 */

		System.out.println("Directorio cambiado a: " + destino.getAbsolutePath());
		return destino;
	}

	public static void main(String[] args) {

		/*
		 * Inicializamos el escaner para poder recibir el promt de nuestra minishell en
		 * caso de que no recibamos nada por teclado se asignara un promt por defecto
		 */

		Scanner sc = new Scanner(System.in);
		System.out.println("Introduce el nombre que quieres que salga en la shell:");
		String promt = sc.nextLine();
		if (promt.isEmpty())
			promt = "default";

		/*
		 * Bucle infinito que nos permite lo primero: 1.- Ver nuestro promt de un color
		 * llamativo a través de la expresión: \u001B[32m siguiendo de >\u001B[0m para
		 * que todo lo que vaya a continuacion se vea blanco de nuevo 2.- Recibimos los
		 * comandos leyendo el teclado a través de la clase escaner, utilizamos trim
		 * para eliminar espacios y que estos no den problemas. 3.- En el caso de que el
		 * usuario ingrese la palabra "exit" independientemente de que sea en mayusculas
		 * o minusculas la shell termine.
		 */

		while (true) {
			System.out.print("\u001B[32m" + promt + "@>\u001B[0m ");
			String comando = sc.nextLine().trim();

			if (comando.equalsIgnoreCase("exit")) {
				System.out.println("Saliendo del minishell, ¡hasta la próxima!");
				break;
			}

			/*
			 * Llamamos al metodo ejecutarLinea para ejecutar los comandos que vengan, al
			 * estar dentro del bucle while(true) este se ejecutará y volverá a salir el
			 * promt pidiendo más comandos.
			 */

			ejecutarLinea(comando);
		}

		/*
		 * Cerramos el objeto Escaner.
		 */

		sc.close();
	}
}
