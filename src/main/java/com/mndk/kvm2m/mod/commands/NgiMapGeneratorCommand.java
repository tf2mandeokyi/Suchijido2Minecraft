package com.mndk.kvm2m.mod.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.mndk.kvm2m.core.vectormap.VectorMapParserResult;
import com.mndk.kvm2m.core.vectorparser.NgiMapParser;

import net.minecraft.command.CommandException;

public class NgiMapGeneratorCommand extends VectorMapGeneratorCommand {


	@Override
	public String getName() {
		return "genngimap";
	}


	@Override
	public VectorMapParserResult fileDataToParserResult(File file) throws CommandException {
		try {
            return NgiMapParser.parse(file);
        } catch(FileNotFoundException exception) {
        	throw new CommandException("File does not exist!");
        } catch(IOException exception) {
            throw new CommandException("There was an error while parsing .dxf map.");
        }
	}


	@Override
	public String getExtension() {
		return "ngi";
	}
	
	
}
