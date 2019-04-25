package com.isentia.util;

import com.isentia.dao.ECDAO;

public class CKRemoveSpam {

	public static void main (String [] args) throws Exception{
		ECDAO ecdao = new ECDAO();
		ecdao.createMasterTicketConnection();
		ecdao.checkForSpamEnglish(args[0], args[1], args[2], args[3]);
	}
}
