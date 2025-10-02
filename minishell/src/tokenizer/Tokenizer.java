package tokenizer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Tokenizer {

    public static TLine tokenize(String input) throws MissingFileException {
        TLine tline = new TLine();

        // Eliminar espacios al principio y al final de la línea de entrada
        input = input.trim();

        // Comprobar si la línea está vacía después de eliminar espacios
        if (input.isEmpty()) {
            return null; // Si la línea está vacía, no hay nada que tokenizar
        }

        // Dividir la línea de comando en partes con posibles tuberías
        String[] pipelineCommands = input.split("\\|");

        for (String commandSegment : pipelineCommands) {
            // Limpiar cada segmento de la tubería y comprobar si no está vacío
            commandSegment = commandSegment.trim();
            if (commandSegment.isEmpty()) {
                continue;  // Si el segmento está vacío, lo saltamos
            }

            // Expresión regular mejorada para capturar las redirecciones, tuberías y argumentos
            Pattern pattern = Pattern.compile("([\"'].*?[\"']|2>>?|[<>|&]{1,2}|[^\\s\"'<>|&]+)");
            Matcher matcher = pattern.matcher(commandSegment);

            // Lista de tokens obtenidos de la entrada
            List<String> tokens = new ArrayList<>();
            while (matcher.find()) {
                tokens.add(matcher.group());
            }

            List<String> argv = new ArrayList<>();
            String filename = null;

            for (int i = 0; i < tokens.size(); i++) {
                String token = tokens.get(i);

                switch (token) {
                    case "<<":  // Redirección de entrada especial (heredoc)
                        if (i + 1 < tokens.size()) {
                            tline.redirectInput = tokens.get(++i);
                        } else {
                            throw new MissingFileException("Missing file for input redirection <<");
                        }
                        break;
                    case "<":  // Redirección de entrada simple
                        if (i + 1 < tokens.size()) {
                            tline.redirectInput = tokens.get(++i);
                        } else {
                            throw new MissingFileException("Missing file for input redirection <");
                        }
                        break;
                    case ">>":  // Redirección de salida en modo append
                        if (i + 1 < tokens.size()) {
                            tline.redirectOutput = tokens.get(++i);  // El siguiente token es el archivo de salida
                            tline.setAppendOutput(true);
                        } else {
                            throw new MissingFileException("Missing file for output redirection >>");
                        }
                        break;
                    case ">":  // Redirección de salida simple
                        if (i + 1 < tokens.size()) {
                            tline.redirectOutput = tokens.get(++i);  // El siguiente token es el archivo de salida
                            tline.setAppendOutput(false);
                        } else {
                            throw new MissingFileException("Missing file for output redirection >");
                        }
                        break;
                    case "2>>":  // Redirección de errores en modo append
                        if (i + 1 < tokens.size()) {
                            tline.redirectError = tokens.get(++i);
                            tline.setAppendError(true);  // Indicar que es append
                        } else {
                            throw new MissingFileException("Missing file for error redirection 2>>");
                        }
                        break;
                    case "2>":  // Redirección de errores simple
                        if (i + 1 < tokens.size()) {
                            tline.redirectError = tokens.get(++i);
                            tline.setAppendError(false);  // Indicar que no es append
                        } else {
                            throw new MissingFileException("Missing file for error redirection 2>");
                        }
                        break;
                    case "&":  // Proceso en segundo plano (background)
                        tline.background = true;
                        break;
                    default:
                        if (filename == null) {
                            filename = token;  // El primer token es el nombre del comando
                        }
                        argv.add(token);  // Agregar el token a los argumentos
                        break;
                }
            }

            // Crear un comando y agregarlo a la estructura de la línea de comandos
            if (filename != null && !argv.isEmpty()) {  // Evitar comandos vacíos
                TCommand command = new TCommand(filename, argv);
                tline.addCommand(command);
            }
        }

        return tline;
    }
}
