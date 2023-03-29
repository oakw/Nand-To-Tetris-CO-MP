package nand.jack_analyzer.process;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import static nand.jack_analyzer.process.KeywordEnum.*;
import static nand.jack_analyzer.process.SymbolEnum.*;

/**
 * Responsible for XML compilation and conversion
 * TODO: Create comments for improved readability
 */
public class CompilationEngine {
    private final ArrayList<Token> tokens;
    private int tokenIndex = 0;
    private Token thisToken;
    public Document doc;

    // routine and subRoutineBody is used recursively, yet independently, so should be accessible in global scope
    private Element routine;
    private Element subRoutineBody;

    public CompilationEngine(JackTokenizer jackTokenizer) throws ParserConfigurationException {
        this.tokens = jackTokenizer.tokens;
        thisToken = tokens.get(tokenIndex);
        setNewDocument();
    }

    /**
     * Creates a new XML document. Used when a new file is parsed
     */
    public void setNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.newDocument();
    }

    /**
     * Transform prepared doc to the .xml file
     */
    public void transformToFile() throws TransformerException {
        File outputFile = new File(thisToken.originFilePath.replace(".jack", ".xml"));

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "html");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

        // Built in parser creates an XML which does not comply to the text comparer
        try {
            RandomAccessFile input = new RandomAccessFile(outputFile, "rw");
            RandomAccessFile output = new RandomAccessFile(outputFile, "rw");
            long length = 0;

            while (input.getFilePointer() < input.length()) {
                String line = input.readLine();
                if (line.contains("parameterList></parameterList")
                    || line.contains("expressionList></expressionList")) {
                    line = line.replace("></", ">\n</");
                }
                line = line + (input.getFilePointer() + 1 == input.length() ? "" : "\n");
                length += line.length();
                output.writeBytes(line);
            }
            output.setLength(length);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Move to the next saved character
     * @return Token The next character
     */
    private Token nextToken() {
        tokenIndex++;
        thisToken = tokens.get(tokenIndex);
        return thisToken;
    }

    /**
     * Move to the previous saved character
     * @return Token The previous character
     */
    private Token previousToken() {
        tokenIndex--;
        thisToken = tokens.get(tokenIndex);
        return thisToken;
    }

    /**
     * Compile the whole token stack. It may include tokens from several files
     * @throws Exception Parsing error
     */
    public void compileAll() throws Exception {
        try {
            boolean firstCompile = true;
            while (tokenIndex + 1 < tokens.size()) {
                if (!firstCompile) {
                    nextToken();
                }
                if (thisToken.getClass() == Keyword.class && ((Keyword) thisToken).type == K_CLASS) {
                    firstCompile = false;
                    compileClass();
                    transformToFile();
                    setNewDocument();
                }
            }
        } catch (Exception e) {
            System.err.println("Error while parsing at line " + (thisToken == null ? "undefined" : thisToken.lineNumber));
            e.printStackTrace();
            // For testing purposes Exception is thrown anyways
            throw new Exception("Parsing error");
        }
    }

    /**
     * Compiles class definition to doc root
     */
    public void compileClass() {
        Element classToCompile = doc.createElement("class");
        addKeyword(classToCompile, thisToken);
        addIdentifier(classToCompile, nextToken());
        addSymbol(classToCompile, nextToken());
        nextToken();
        compileClassVarDec(classToCompile);
        compileSubRoutine(classToCompile);
        nextToken();
        addSymbol(classToCompile, thisToken);

        doc.appendChild(classToCompile);
    }

    /**
     * Compiles var definitions of the class
     * @param parent Where to append the current element in XML
     */
    public void compileClassVarDec(Element parent) {
        while (thisToken.getClass() == Keyword.class && (((Keyword) thisToken).type == K_FIELD || ((Keyword) thisToken).type == K_STATIC)) {
            Element classVarDecToCompile = doc.createElement("classVarDec");
            addKeyword(classVarDecToCompile, thisToken);

            if (nextToken().getClass() == Keyword.class) {
                addKeyword(classVarDecToCompile, thisToken);
            } else {
                addIdentifier(classVarDecToCompile, thisToken);
            }

            addIdentifier(classVarDecToCompile, nextToken());
            nextToken();

            if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
                addSymbol(classVarDecToCompile, thisToken);
                addIdentifier(classVarDecToCompile, nextToken());
                nextToken();
            }

            addSymbol(classVarDecToCompile, thisToken);

            parent.appendChild(classVarDecToCompile);
            nextToken();
        }
    }

    /**
     * Compiles a subroutine the class may have
     * @param parent Where to append the current element in XML
     */
    public void compileSubRoutine(Element parent) {
        // Stops the recursive call when } reached
        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_RIGHTBRACKET) {
            previousToken();
            return;
        }

        if (thisToken.getClass() == Keyword.class
                && (((Keyword) thisToken).type == K_FUNCTION
                || ((Keyword) thisToken).type == K_CONSTRUCTOR
                || ((Keyword) thisToken).type == K_METHOD)
        ) {
            routine = doc.createElement("subroutineDec");

            addKeyword(routine, thisToken);
            nextToken();
        }

        if (thisToken.getClass() == Keyword.class) {
            addKeyword(routine, thisToken);
            nextToken();

        } else if (thisToken.isOfType(Identifier.class)) {
            addIdentifier(routine, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Identifier.class)) {
            addIdentifier(routine, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACET) {
            compileParameterList(routine);
            nextToken();
        }

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACKET) {
            subRoutineBody = doc.createElement("subroutineBody");
            addSymbol(subRoutineBody, thisToken);
            nextToken();
        }

        while(thisToken.getClass() == Keyword.class && ((Keyword) thisToken).type == K_VAR) {
            compileVarDec(subRoutineBody);
        }

        Element statements = doc.createElement("statements");
        compileStatements(statements);
        subRoutineBody.appendChild(statements);
        if (thisToken.isOfType(Symbol.class)) {
            addSymbol(subRoutineBody, thisToken);
            nextToken();
        }
        routine.appendChild(subRoutineBody);
        parent.appendChild(routine);

        compileSubRoutine(parent);
    }

    /**
     * Compiles a list of parameters. Could be also empty
     * @param parent Where to append the current element in XML
     */
    public void compileParameterList(Element parent) {
        Element parametersList = doc.createElement("parameterList");
        addSymbol(parent, thisToken);
        nextToken();

        while (thisToken.getClass() != Symbol.class || ((Symbol) thisToken).symbolType != SymbolEnum.S_RIGHTBRACET) {
            if (thisToken.isOfType(Identifier.class)) {
                addIdentifier(parametersList, thisToken);
                nextToken();

            } else if (thisToken.getClass() == Keyword.class) {
                addKeyword(parametersList, thisToken);
                nextToken();

            } else if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
                addSymbol(parametersList, thisToken);
                nextToken();
            }
        }

        parent.appendChild(parametersList);
        addSymbol(parent, thisToken);
    }

    /**
     * Compiles var declaration in the subroutine
     */
    public void compileVarDec(Element parent) {
        Element varDecs = doc.createElement("varDec");
        addKeyword(varDecs, thisToken);
        nextToken();

        if (thisToken.isOfType(Keyword.class)) {
            addKeyword(varDecs, thisToken);
            nextToken();
        } else if (thisToken.isOfType(Identifier.class)) {
            addIdentifier(varDecs, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Identifier.class)) {
            addIdentifier(varDecs, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
            addSymbol(varDecs, thisToken);
            nextToken();
            addIdentifier(varDecs, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_SEMICOLON) {
            addSymbol(varDecs, thisToken);
            nextToken();
        }

        parent.appendChild(varDecs);
    }

    /**
     * Compiles do/let/if/while/return
     * @param parent Where to append the current element in XML
     */
    public void compileStatements(Element parent) {

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_RIGHTBRACKET) {
            return;

        } else if (thisToken.isOfType(Keyword.class)) {
            switch (((Keyword) thisToken).type) {
                case K_DO -> compileDo(parent);
                case K_LET -> compileLet(parent);
                case K_IF -> compileIf(parent);
                case K_WHILE -> compileWhile(parent);
                case K_RETURN -> compileReturn(parent);
            }

        } else {
            nextToken();
        }

        compileStatements(parent);
    }

    public void compileDo(Element parent) {
        Element doBlock = doc.createElement("doStatement");

        if (thisToken.isOfType(Keyword.class) && ((Keyword) thisToken).type == K_DO) {
            addKeyword(doBlock, thisToken);
        }
        compileCall(doBlock);
        addSymbol(doBlock, thisToken);
        nextToken();

        parent.appendChild(doBlock);
    }

    public void compileIf(Element parent) {
        Element ifStatement = doc.createElement("ifStatement");

        addKeyword(ifStatement, thisToken);
        addSymbol(ifStatement, nextToken());
        compileExpression(ifStatement);

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == S_RIGHTBRACET) {
            nextToken();
            addSymbol(ifStatement, thisToken);

        } else {
            addSymbol(ifStatement, nextToken());
        }

        addSymbol(ifStatement, nextToken());
        Element statements = doc.createElement("statements");
        compileStatements(statements);
        ifStatement.appendChild(statements);
        addSymbol(ifStatement, thisToken);
        nextToken();

        if (thisToken.isOfType(Keyword.class) && ((Keyword) thisToken).type == K_ELSE) {
            addKeyword(ifStatement, thisToken);
            addSymbol(ifStatement, nextToken());
            nextToken();
            Element statements1 = doc.createElement("statements");
            compileStatements(statements1);
            ifStatement.appendChild(statements1);
            addSymbol(ifStatement, thisToken);
            nextToken();
        }

        parent.appendChild(ifStatement);
    }

    public void compileLet(Element parent) {
        Element letStatement = doc.createElement("letStatement");

        addKeyword(letStatement, thisToken);
        addIdentifier(letStatement, nextToken());
        nextToken();

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == S_LEFTSQUAREBRACKET) {
            addSymbol(letStatement, thisToken);
            compileExpression(letStatement);
            nextToken();
            addSymbol(letStatement, thisToken);
            nextToken();
        }

        addSymbol(letStatement, thisToken);

        compileExpression(letStatement);

        if (thisToken.isOfType(Symbol.class) && (((Symbol) thisToken).symbolType == S_RIGHTBRACET || ((Symbol) thisToken).symbolType == S_RIGHTSQUAREBRACKET)) {
            nextToken();
        }

        if (! thisToken.isOfType(Symbol.class)) {
            nextToken();
        }

        addSymbol(letStatement, thisToken);

        parent.appendChild(letStatement);
    }

    public void compileWhile(Element parent) {
        Element whileStatement = doc.createElement("whileStatement");

        addKeyword(whileStatement, thisToken);
        addSymbol(whileStatement, nextToken());
        compileExpression(whileStatement);
        nextToken();

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == S_LEFTBRACKET) {
            previousToken();
        }

        addSymbol(whileStatement, thisToken);
        addSymbol(whileStatement, nextToken());
        Element statements = doc.createElement("statements");
        compileStatements(statements);
        whileStatement.appendChild(statements);
        addSymbol(whileStatement, thisToken);
        nextToken();

        parent.appendChild(whileStatement);
    }

    public void compileReturn(Element parent) {
        Element returnStatement = doc.createElement("returnStatement");
        addKeyword(returnStatement, thisToken);
        nextToken();

        if (thisToken.isOfType(Symbol.class)) {
            if (((Symbol) thisToken).symbolType == S_SEMICOLON) {
                addSymbol(returnStatement, thisToken);

            } else {
                compileExpression(returnStatement);
                nextToken();
            }

        } else {
            previousToken();
            compileExpression(returnStatement);
            nextToken();
            addSymbol(returnStatement, thisToken);
        }

        parent.appendChild(returnStatement);
    }

    public void compileCall(Element parent) {
        nextToken();

        if (thisToken.isOfType(Identifier.class)) {
            addIdentifier(parent, thisToken);
            nextToken();
        }

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == S_LEFTBRACET) {
            addSymbol(parent, thisToken);
            nextToken();
            compileExpressionList(parent);
            nextToken();
            addSymbol(parent, thisToken);
            nextToken();

        } else if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_DOT) {
            addSymbol(parent, thisToken);
            nextToken();
            addIdentifier(parent, thisToken);
            nextToken();
            addSymbol(parent, thisToken);
            nextToken();
            compileExpressionList(parent);
            nextToken();
            if (thisToken.isOfType(Identifier.class)) {
                previousToken();
            }
            addSymbol(parent, thisToken);
            nextToken();
        }
    }

    /**
     * Compiles a list of expressions. There may be several ones
     * @param parent Where to append the current element in XML
     */
    public void compileExpressionList(Element parent) {
        Element expressionList = doc.createElement("expressionList");

        if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_RIGHTBRACET) {
            previousToken();
        } else {
            previousToken();
            compileExpression(expressionList);
        }

        while (true) {
            nextToken();
            if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_COMMA) {
                addSymbol(expressionList, thisToken);
                compileExpression(expressionList);

            } else if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == S_RIGHTBRACET) {
                previousToken();
                break;
            }
        }

        parent.appendChild(expressionList);
    }

    /**
     * Compiles a single expression
     * @param parent Where to append the current element in XML
     */
    public void compileExpression(Element parent) {
        Element expression = doc.createElement("expression");
        compileTerm(expression);

        while(true) {
            nextToken();
            if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType.isOperand) {
                addSymbol(expression, thisToken);
                compileTerm(expression);
            } else {
                previousToken();
                break;
            }
        }

        parent.appendChild(expression);
    }

    /**
     * Compiles a single term
     * @param parent Where to append the current element in XML
     */
    public void compileTerm(Element parent) {
        Element term = doc.createElement("term");
        nextToken();

        if (thisToken.isOfType(Identifier.class)) {
            Identifier preservedIdentifier = (Identifier) thisToken;
            nextToken();

            if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTSQUAREBRACKET) {
                addIdentifier(term, preservedIdentifier);
                addSymbol(term, thisToken);
                compileExpression(term);
                nextToken();
                addSymbol(term, thisToken);

            } else if (thisToken.isOfType(Symbol.class) && (((Symbol) thisToken).symbolType == SymbolEnum.S_DOT || ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACET)) {
                previousToken();
                previousToken();
                compileCall(term);

            } else {
                addIdentifier(term, preservedIdentifier);
                previousToken();
            }
        } else {
            if (thisToken.isOfType(IntegerConstant.class)){
                addCustomElement(term, "integerConstant", String.valueOf(((IntegerConstant) thisToken).constant));

            } else if (thisToken.isOfType(StringConstant.class)) {
                addCustomElement(term, "stringConstant", String.valueOf(((StringConstant) thisToken).constant));

            } else if (thisToken.isOfType(Keyword.class) && (Arrays.asList(K_THIS, K_TRUE, K_FALSE, K_NULL)).contains(((Keyword) thisToken).type)) {
                addKeyword(term, thisToken);

            } else if (thisToken.isOfType(Symbol.class) && ((Symbol) thisToken).symbolType == SymbolEnum.S_LEFTBRACET) {
                addSymbol(term, thisToken);
                compileExpression(term);
                nextToken();
                addSymbol(term, thisToken);

            } else if (thisToken.isOfType(Symbol.class) && (Arrays.asList(S_TILDE, S_MINUS)).contains(((Symbol) thisToken).symbolType)) {
                addSymbol(term, thisToken);
                compileTerm(term);
            }
        }

        parent.appendChild(term);
    }

    /**
     * Add an identifier to the XML file
     * @param parent Where to append the current element in XML
     * @param identifier Identifier of type Token to add
     */
    public void addIdentifier(Element parent, Token identifier) {
        // Incorrect element class will notify of possible syntax error
        if (identifier.getClass() != Identifier.class) {
            throw new IllegalArgumentException("Identifier expected at line " + identifier.lineNumber + " in file " + identifier.originFilePath + " " + identifier.getClass() + " given");
        }
        Element identifierElement = doc.createElement("identifier");
        identifierElement.appendChild(doc.createTextNode(((Identifier) identifier).name));
        parent.appendChild(identifierElement);
    }

    /**
     * Add a keyword to the XML file
     * @param parent Where to append the current element in XML
     * @param keyword Keyword of type Keyword to add
     */
    public void addKeyword(Element parent, Token keyword) {
        // Incorrect element class will notify of possible syntax error
        if (keyword.getClass() != Keyword.class) {
            throw new IllegalArgumentException("Keyword expected at line " + keyword.lineNumber + " in file " + keyword.originFilePath + " " + keyword.getClass() + " given");
        }
        Element keywordElement = doc.createElement("keyword");
        keywordElement.appendChild(doc.createTextNode(keyword.named));
        parent.appendChild(keywordElement);
    }

    /**
     * Add a custom element to the XML
     * @param parent Where to append the current element in XML
     * @param tagName Custom tag name
     * @param tagValue Value of the created custom tag
     */
    public void addCustomElement(Element parent, String tagName, String tagValue) {
        Element customElement = doc.createElement(tagName);
        customElement.appendChild(doc.createTextNode(tagValue));
        parent.appendChild(customElement);
    }

    /**
     * Add a symbol to the XML file
     * @param parent Where to append the current element in XML
     * @param symbol Symbol of type Symbol to add
     */
    public void addSymbol(Element parent, Token symbol) {
        // Incorrect element class will notify of possible syntax error
        if (symbol.getClass() != Symbol.class) {
            throw new IllegalArgumentException("Symbol expected at line " + symbol.lineNumber + " in file " + symbol.originFilePath + " " + symbol.getClass() + " given");
        }
        Element symbolElement = doc.createElement("symbol");
        symbolElement.appendChild(doc.createTextNode(String.valueOf(((Symbol) symbol).symbolType.label)));
        parent.appendChild(symbolElement);
    }
}
